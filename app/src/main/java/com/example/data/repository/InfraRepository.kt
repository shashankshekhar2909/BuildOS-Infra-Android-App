package com.example.data.repository

import com.example.data.api.BuildOsApiService
import com.example.data.datastore.SessionManager
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

class InfraRepository(private val sessionManager: SessionManager) {

    // Helper formatter for generated timestamps
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    // In-memory state for Sandbox Demo Mode
    private val _nodes = MutableStateFlow<List<Node>>(emptyList())
    val nodes: StateFlow<List<Node>> get() = _nodes

    private val _containers = MutableStateFlow<List<Container>>(emptyList())
    val containers: StateFlow<List<Container>> get() = _containers

    private val _zones = MutableStateFlow<List<CloudflareZone>>(emptyList())
    val zones: StateFlow<List<CloudflareZone>> get() = _zones

    private val _dnsRecords = MutableStateFlow<Map<String, List<DnsRecord>>>(emptyMap()) // zoneId -> records
    val dnsRecords: StateFlow<Map<String, List<DnsRecord>>> get() = _dnsRecords

    private val _logs = MutableStateFlow<List<SystemLog>>(emptyList())
    val logs: StateFlow<List<SystemLog>> get() = _logs

    private val _lockdownActive = MutableStateFlow(false)
    val lockdownActive: StateFlow<Boolean> get() = _lockdownActive

    private val _proxmoxGuests = MutableStateFlow<Map<String, List<ProxmoxGuest>>>(emptyMap()) // nodeId -> guests

    init {
        // Spin up premium, high-quality initial seed data for the offline/demo sandbox
        resetSandboxData()
    }

    fun resetSandboxData() {
        _lockdownActive.value = false
        _nodes.value = emptyList()
        _containers.value = emptyList()
        _zones.value = emptyList()
        _dnsRecords.value = emptyMap()
        _proxmoxGuests.value = emptyMap()
        _logs.value = listOf(
            SystemLog("log-status", getFormattedTime(0), "INFO", "console-link", "System initialized. No fleet links active. Connect base URL in configuration settings.")
        )
    }

    private fun getFormattedTime(offsetSeconds: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, -offsetSeconds * 30)
        return timeFormat.format(calendar.time)
    }

    private fun addLogEvent(level: String, source: String, msg: String) {
        val nextId = "log-${Random.nextInt(1000, 9999)}"
        val timeStr = timeFormat.format(Date())
        val newLog = SystemLog(nextId, timeStr, level, source, msg)
        _logs.value = listOf(newLog) + _logs.value.take(49) // limit to recent 50
    }

    private fun backendBaseUrl(): String {
        return sessionManager.baseUrlFlow.first().removeSuffix("/") + "/"
    }

    // Dynamic creator for client instances matching current base URL config.
    private suspend fun getApiService(): Pair<BuildOsApiService, String> {
        val url = backendBaseUrl()
        val token = sessionManager.tokenFlow.first() ?: ""
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        return Pair(retrofit.create(BuildOsApiService::class.java), "Bearer $token")
    }

    // -- Authentication
    suspend fun login(req: LoginRequest): Result<LoginResponse> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            // Check credentials instantly for demo robustness
            if (req.username.isNotBlank() && req.password.isNotBlank()) {
                val response = LoginResponse("mock_token_${Random.nextInt(99999)}", req.username, req.role)
                sessionManager.saveSession(response.token, response.username, response.role)
                addLogEvent("INFO", "auth-manager", "Demo Mode User '${req.username}' authenticated as ${req.role}")
                return Result.success(response)
            } else {
                return Result.failure(Exception("Username and Password are required!"))
            }
        }

        return try {
            val (service, _) = getApiService()
            val response = service.login(req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveSession(body.token, body.username, body.role)
                Result.success(body)
            } else {
                Result.failure(Exception("Login failed: ${response.message()} (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkBackendHealth(): Result<BackendHealth> {
        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl(backendBaseUrl())
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                        .build()
                )
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            val service = retrofit.create(BuildOsApiService::class.java)
            val response = service.getHealth()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Health check failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -- Nodes list
    suspend fun fetchNodes(): Result<List<Node>> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            return Result.success(_nodes.value)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.getNodes(auth)
            if (res.isSuccessful && res.body() != null) {
                _nodes.value = res.body()!!
                Result.success(_nodes.value)
            } else {
                Result.failure(Exception("Failed to fetch nodes: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create / Register node
    suspend fun registerNode(name: String, type: String, provider: String, ip: String, region: String): Result<Node> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            val nextId = "node-demo-${Random.nextInt(100, 999)}"
            val agentToken = "bo_node_agent_tok_${Random.nextInt(100000, 999999)}"
            val installCommand = "curl -fsSL https://os.buildwithshashank.com/install.sh | bash -s -- --token $agentToken"
            val node = Node(
                id = nextId,
                name = name,
                type = type,
                provider = provider,
                ip = ip,
                region = region,
                status = "online",
                lastPing = "Just registered",
                agentToken = agentToken,
                installCommand = installCommand
            )
            _nodes.value = _nodes.value + node
            addLogEvent("INFO", "system-core", "Registered server node '$name' of type '$type'")

            // Initialize empty proxmox guests list if type is PROXMOX
            if (type == "PROXMOX") {
                _proxmoxGuests.value = _proxmoxGuests.value + (nextId to listOf(
                    ProxmoxGuest(vmid = 200, name = "pve-template-docker", status = "stopped", type = "qemu", maxmem = 4294967296L),
                    ProxmoxGuest(vmid = 201, name = "pve-ubuntu-lxc", status = "running", type = "lxc", cpu = 0.2, mem = 128000000L, maxmem = 1073741824L)
                ))
            }

            return Result.success(node)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.registerNode(auth, CreateNodeRequest(name, type, provider, ip, region))
            if (res.isSuccessful && res.body() != null) {
                val node = res.body()!!
                _nodes.value = _nodes.value + node
                Result.success(node)
            } else {
                Result.failure(Exception("Failed to register node: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Edit node (patch)
    suspend fun updateNode(id: String, name: String, ip: String, region: String): Result<Node> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            _nodes.value = _nodes.value.map {
                if (it.id == id) {
                    it.copy(name = name, ip = ip, region = region)
                } else it
            }
            addLogEvent("INFO", "system-core", "Updated configuration settings for node ID '$id'")
            return Result.success(_nodes.value.first { it.id == id })
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.updateNode(auth, id, mapOf("name" to name, "ip" to ip, "region" to region))
            if (res.isSuccessful && res.body() != null) {
                val n = res.body()!!
                _nodes.value = _nodes.value.map { if (it.id == id) n else it }
                Result.success(n)
            } else {
                Result.failure(Exception("Failed to update node: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete node
    suspend fun deleteNode(id: String): Result<Boolean> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            val nodeName = _nodes.value.find { it.id == id }?.name ?: id
            _nodes.value = _nodes.value.filterNot { it.id == id }
            addLogEvent("WARN", "system-core", "Deleted infrastructure node: $nodeName")
            return Result.success(true)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.deleteNode(auth, id)
            if (res.isSuccessful) {
                _nodes.value = _nodes.value.filterNot { it.id == id }
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete node: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Rotate Node token
    suspend fun rotateNodeToken(id: String): Result<Node> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            val nextToken = "bo_node_agent_tok_rot_${Random.nextInt(100000, 999999)}"
            _nodes.value = _nodes.value.map {
                if (it.id == id) {
                    it.copy(
                        agentToken = nextToken,
                        installCommand = "curl -fsSL https://os.buildwithshashank.com/install.sh | bash -s -- --token $nextToken"
                    )
                } else it
            }
            addLogEvent("WARN", "node-security", "Rotated agent authorization token for node: ${_nodes.value.find { it.id == id }?.name}")
            return Result.success(_nodes.value.first { it.id == id })
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.regenerateNodeToken(auth, id)
            if (res.isSuccessful && res.body() != null) {
                val n = res.body()!!
                _nodes.value = _nodes.value.map { if (it.id == id) n else it }
                Result.success(n)
            } else {
                Result.failure(Exception("Failed to rotate token: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -- Containers
    suspend fun fetchContainers(): Result<List<Container>> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            return Result.success(_containers.value)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.getContainers(auth)
            if (res.isSuccessful && res.body() != null) {
                _containers.value = res.body()!!
                Result.success(_containers.value)
            } else {
                Result.failure(Exception("Failed to fetch containers: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Control Containers
    suspend fun controlContainer(id: String, action: String): Result<Boolean> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            val targetState = when (action) {
                "start" -> "running"
                "stop" -> "stopped"
                "restart" -> "running"
                else -> "running"
            }
            _containers.value = _containers.value.map {
                if (it.id == id) {
                    it.copy(state = targetState)
                } else it
            }
            val targetName = _containers.value.find { it.id == id }?.name ?: id
            addLogEvent("INFO", "container-manager", "Manual override '$action' dispatched successfully to container '$targetName'")
            return Result.success(true)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.controlContainer(auth, id, ContainerControlRequest(action))
            if (res.isSuccessful) {
                val targetState = if (action == "stop") "stopped" else "running"
                _containers.value = _containers.value.map {
                    if (it.id == id) it.copy(state = targetState) else it
                }
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to control container: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Toggle Auto-heal
    suspend fun toggleAutoHeal(id: String, enabled: Boolean): Result<Boolean> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            _containers.value = _containers.value.map {
                if (it.id == id) it.copy(autoHeal = enabled) else it
            }
            val targetName = _containers.value.find { it.id == id }?.name ?: id
            addLogEvent("INFO", "auto-heal-daemon", "Auto heal state set to $enabled for container '$targetName'")
            return Result.success(true)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.setContainerAutoHeal(auth, id, ContainerAutoHealRequest(enabled))
            if (res.isSuccessful) {
                _containers.value = _containers.value.map {
                    if (it.id == id) it.copy(autoHeal = enabled) else it
                }
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update auto-heal: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -- Cloudflare Domains
    suspend fun fetchZones(): Result<List<CloudflareZone>> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            return Result.success(_zones.value)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.getCloudflareZones(auth)
            if (res.isSuccessful && res.body() != null) {
                _zones.value = res.body()!!
                Result.success(_zones.value)
            } else {
                Result.failure(Exception("Failed to fetch zones: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkZoneAccess(zoneId: String): Result<String> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            val zoneName = _zones.value.find { it.id == zoneId }?.name ?: "Unknown"
            addLogEvent("INFO", "dns-syncer", "Verified zone access API communication for $zoneName - Response Code 200 OK")
            return Result.success("API access verified successfully. DNS records writable.")
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.checkZoneAccess(auth, zoneId)
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body().toString())
            } else {
                Result.failure(Exception("Failed zone access check: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchLiveDnsRecordsForDomainForSandbox(zoneId: String, domainName: String): List<DnsRecord> {
        val recordTypes = listOf("A", "CNAME", "MX", "TXT", "AAAA", "NS")
        val results = mutableListOf<DnsRecord>()
        var counter = 1
        for (type in recordTypes) {
            val res = resolveLiveDns(domainName, type)
            res.onSuccess { liveRecords ->
                liveRecords.forEach { live ->
                    val cleanContent = live.data.removeSuffix(".")
                    results.add(
                        DnsRecord(
                            id = "rec-live-${zoneId}-${type}-${counter++}",
                            zoneId = zoneId,
                            name = live.name,
                            type = live.type,
                            content = cleanContent,
                            ttl = live.ttl,
                            proxied = false
                        )
                    )
                }
            }
        }
        return results
    }

    suspend fun fetchDnsRecords(zoneId: String): Result<List<DnsRecord>> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            val zone = _zones.value.find { it.id == zoneId }
            if (zone != null) {
                val currentRecords = _dnsRecords.value[zoneId] ?: emptyList()
                val hasAnyLive = currentRecords.any { it.id.startsWith("rec-live-") }
                if (!hasAnyLive && zone.name.isNotBlank()) {
                    val liveRecords = fetchLiveDnsRecordsForDomainForSandbox(zoneId, zone.name)
                    if (liveRecords.isNotEmpty()) {
                        _dnsRecords.value = _dnsRecords.value + (zoneId to liveRecords)
                        addLogEvent("INFO", "dns-syncer", "Live resolved ${liveRecords.size} real DNS records from internet nameservers for zone: ${zone.name}")
                        return Result.success(liveRecords)
                    }
                }
            }
            return Result.success(_dnsRecords.value[zoneId] ?: emptyList())
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.getDnsRecords(auth, zoneId)
            if (res.isSuccessful && res.body() != null) {
                val records = res.body()!!
                _dnsRecords.value = _dnsRecords.value + (zoneId to records)
                Result.success(records)
            } else {
                Result.failure(Exception("Failed to fetch dns: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDnsRecord(zoneId: String, name: String, type: String, content: String): Result<DnsRecord> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            val nextId = "rec-demo-${Random.nextInt(500, 999)}"
            val entry = DnsRecord(id = nextId, zoneId = zoneId, name = name, type = type, content = content)
            val currentList = _dnsRecords.value[zoneId] ?: emptyList()
            _dnsRecords.value = _dnsRecords.value + (zoneId to (currentList + entry))
            addLogEvent("INFO", "dns-syncer", "Created DNS Record '$name' type $type -> $content successful.")
            return Result.success(entry)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.createDnsRecord(auth, CreateDnsRecordRequest(zoneId, name, type, content))
            if (res.isSuccessful && res.body() != null) {
                val record = res.body()!!
                val currentList = _dnsRecords.value[zoneId] ?: emptyList()
                _dnsRecords.value = _dnsRecords.value + (zoneId to (currentList + record))
                Result.success(record)
            } else {
                Result.failure(Exception("Failed to create DNS: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDnsRecord(zoneId: String, recordId: String): Result<Boolean> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            val currentList = _dnsRecords.value[zoneId] ?: emptyList()
            val targetName = currentList.find { it.id == recordId }?.name ?: recordId
            _dnsRecords.value = _dnsRecords.value + (zoneId to currentList.filterNot { it.id == recordId })
            addLogEvent("WARN", "dns-syncer", "Purged DNS record index: $targetName")
            return Result.success(true)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.deleteDnsRecord(auth, recordId)
            if (res.isSuccessful) {
                val currentList = _dnsRecords.value[zoneId] ?: emptyList()
                _dnsRecords.value = _dnsRecords.value + (zoneId to currentList.filterNot { it.id == recordId })
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete DNS: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add Cloudflare zone
    suspend fun addZoneLater(name: String): Result<CloudflareZone> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            val nextId = "zone-demo-${Random.nextInt(10, 99)}"
            val entry = CloudflareZone(id = nextId, name = name, status = "active", zoneHealth = "Active / Checked")
            _zones.value = _zones.value + entry
            _dnsRecords.value = _dnsRecords.value + (nextId to emptyList())
            addLogEvent("INFO", "dns-syncer", "Added and verified zone: $name")
            return Result.success(entry)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.createZone(auth, mapOf("name" to name))
            if (res.isSuccessful && res.body() != null) {
                val zone = res.body()!!
                _zones.value = _zones.value + zone
                Result.success(zone)
            } else {
                Result.failure(Exception("Failed to create zone: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -- System Logs
    suspend fun fetchLogs(): Result<List<SystemLog>> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            return Result.success(_logs.value)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.getLogs(auth)
            if (res.isSuccessful && res.body() != null) {
                _logs.value = res.body()!!
                Result.success(_logs.value)
            } else {
                Result.failure(Exception("Failed to fetch logs: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -- Emergency Reset Lockdowns
    suspend fun triggerEmergencyKill(): Result<Boolean> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            _lockdownActive.value = true
            // Immediately stop all containers & error state nodes for a realistic dramatic emergency visual feedback
            _containers.value = _containers.value.map { it.copy(state = "stopped") }
            _nodes.value = _nodes.value.map { it.copy(status = "error") }
            addLogEvent("CRITICAL", "security-core", "⚠️ CRITICAL LOCKDOWN SHUTDOWN CALLED BY ADMIN. ALL FLEET AGENTS DISPATCHED STOP CODES.")
            return Result.success(true)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.emergencyKill(auth)
            if (res.isSuccessful && res.body() != null) {
                _lockdownActive.value = res.body()!!.lockdownActive
                Result.success(true)
            } else {
                Result.failure(Exception("Failed emergency action: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun releaseEmergencyReset(): Result<Boolean> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            _lockdownActive.value = false
            // Recover nodes status to pristine health with restart flags
            _nodes.value = _nodes.value.map { if (it.id != "node-abc-4") it.copy(status = "online") else it }
            _containers.value = _containers.value.map { if (it.id != "cont-5") it.copy(state = "running") else it }
            addLogEvent("INFO", "security-core", "Emergency Lockdown state released by Administrator session. Restarting essential daemon pipelines.")
            return Result.success(true)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.emergencyReset(auth)
            if (res.isSuccessful && res.body() != null) {
                _lockdownActive.value = res.body()!!.lockdownActive
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to release lock: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -- Proxmox Guest Controls
    fun getProxmoxGuestsForNode(nodeId: String): List<ProxmoxGuest> {
        return _proxmoxGuests.value[nodeId] ?: emptyList()
    }

    suspend fun fetchProxmoxGuestsRemote(nodeId: String): Result<List<ProxmoxGuest>> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            return Result.success(_proxmoxGuests.value[nodeId] ?: emptyList())
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.getProxmoxGuests(auth, nodeId)
            if (res.isSuccessful && res.body() != null) {
                val guests = res.body()!!
                _proxmoxGuests.value = _proxmoxGuests.value + (nodeId to guests)
                Result.success(guests)
            } else {
                Result.failure(Exception("Failed to fetch cluster guests: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun controlProxmoxGuest(nodeId: String, kind: String, vmid: Int, action: String): Result<Boolean> {
        val isDemo = sessionManager.demoModeFlow.first()
        if (isDemo) {
            val list = _proxmoxGuests.value[nodeId] ?: emptyList()
            val targetState = when (action) {
                "start" -> "running"
                "stop", "shutdown" -> "stopped"
                else -> "running"
            }
            _proxmoxGuests.value = _proxmoxGuests.value + (nodeId to list.map {
                if (it.vmid == vmid) it.copy(status = targetState) else it
            })

            val guestName = list.find { it.vmid == vmid }?.name ?: "$vmid"
            addLogEvent("WARN", "pve-manager", "PVE Cluster Host executed command '$action' on Guest '$guestName' (VMID $vmid)")
            return Result.success(true)
        }

        return try {
            val (service, auth) = getApiService()
            val res = service.controlProxmoxGuest(auth, nodeId, kind, vmid, mapOf("action" to action))
            if (res.isSuccessful) {
                val list = _proxmoxGuests.value[nodeId] ?: emptyList()
                val targetState = if (action == "stop" || action == "shutdown") "stopped" else "running"
                _proxmoxGuests.value = _proxmoxGuests.value + (nodeId to list.map {
                    if (it.vmid == vmid) it.copy(status = targetState) else it
                })
                Result.success(true)
            } else {
                Result.failure(Exception("Failed PVE action: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveLiveDns(domain: String, type: String): Result<List<LiveDnsRecord>> {
        return withContext(Dispatchers.IO) {
            try {
                // We query Google's open JSON resolver
                val url = "https://dns.google/resolve?name=${domain}&type=${type}"
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("HTTP error: ${response.code}"))
                    }
                    val bodyString = response.body?.string() ?: ""
                    val json = JSONObject(bodyString)
                    val recordsList = mutableListOf<LiveDnsRecord>()
                    if (json.has("Answer")) {
                        val answerArray = json.getJSONArray("Answer")
                        for (i in 0 until answerArray.length()) {
                            val obj = answerArray.getJSONObject(i)
                            val name = obj.optString("name", "")
                            val ttl = obj.optInt("TTL", 0)
                            val data = obj.optString("data", "")
                            recordsList.add(
                                LiveDnsRecord(
                                    name = name.removeSuffix("."),
                                    type = type,
                                    ttl = ttl,
                                    data = data
                                )
                            )
                        }
                    }
                    Result.success(recordsList)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
