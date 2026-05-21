package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.SessionManager
import com.example.data.model.*
import com.example.data.repository.InfraRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InfraViewModel(
    private val repository: InfraRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // --- Authentication flows
    val token: StateFlow<String?> = sessionManager.tokenFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val username: StateFlow<String?> = sessionManager.usernameFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val userRole: StateFlow<String?> = sessionManager.userRoleFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val baseUrl: StateFlow<String> = sessionManager.baseUrlFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "https://os.buildwithshashank.com"
    )
    val demoMode: StateFlow<Boolean> = sessionManager.demoModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    // --- State values
    val nodes: StateFlow<List<Node>> = repository.nodes
    val containers: StateFlow<List<Container>> = repository.containers
    val zones: StateFlow<List<CloudflareZone>> = repository.zones
    val dnsRecords: StateFlow<Map<String, List<DnsRecord>>> = repository.dnsRecords
    val logs: StateFlow<List<SystemLog>> = repository.logs
    val lockdownActive: StateFlow<Boolean> = repository.lockdownActive

    private val _backendHealth = MutableStateFlow<BackendHealth?>(null)
    val backendHealth: StateFlow<BackendHealth?> = _backendHealth.asStateFlow()

    private val _lastSyncAt = MutableStateFlow<String?>(null)
    val lastSyncAt: StateFlow<String?> = _lastSyncAt.asStateFlow()

    private val _syncMode = MutableStateFlow("idle")
    val syncMode: StateFlow<String> = _syncMode.asStateFlow()

    private val _healthError = MutableStateFlow<String?>(null)
    val healthError: StateFlow<String?> = _healthError.asStateFlow()

    // UI Loading & Toast states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Active domain zone selection
    private val _selectedZoneId = MutableStateFlow<String>("zone-1")
    val selectedZoneId: StateFlow<String> = _selectedZoneId.asStateFlow()

    // Proxmox guest active display state per node (Id -> List)
    private val _nodeGuests = MutableStateFlow<Map<String, List<ProxmoxGuest>>>(emptyMap())
    val nodeGuests: StateFlow<Map<String, List<ProxmoxGuest>>> = _nodeGuests.asStateFlow()

    // Live DNS query states
    private val _liveDnsQuery = MutableStateFlow("")
    val liveDnsQuery: StateFlow<String> = _liveDnsQuery.asStateFlow()

    private val _liveDnsType = MutableStateFlow("A")
    val liveDnsType: StateFlow<String> = _liveDnsType.asStateFlow()

    private val _liveDnsResults = MutableStateFlow<List<LiveDnsRecord>>(emptyList())
    val liveDnsResults: StateFlow<List<LiveDnsRecord>> = _liveDnsResults.asStateFlow()

    private val _liveDnsLoading = MutableStateFlow(false)
    val liveDnsLoading: StateFlow<Boolean> = _liveDnsLoading.asStateFlow()

    private val _liveDnsError = MutableStateFlow<String?>(null)
    val liveDnsError: StateFlow<String?> = _liveDnsError.asStateFlow()

    private var backgroundSyncJob: Job? = null
    private val syncTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    init {
        startBackgroundSync()
    }

    fun selectZone(zoneId: String) {
        _selectedZoneId.value = zoneId
        viewModelScope.launch {
            _isLoading.value = true
            repository.fetchDnsRecords(zoneId)
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun refreshAll(background: Boolean = false, forceFleetData: Boolean = false) {
        viewModelScope.launch {
            if (!background) {
                _isLoading.value = true
            }
            _errorMessage.value = null
            try {
                val canAccessFleetData = forceFleetData || !token.value.isNullOrBlank() || demoMode.value

                val healthResult = repository.checkBackendHealth()
                healthResult.onSuccess {
                    _backendHealth.value = it
                    _healthError.value = null
                }.onFailure {
                    _healthError.value = it.message ?: "Backend health probe failed"
                }

                if (canAccessFleetData) {
                    val nodeResult = repository.fetchNodes()
                    val containerResult = repository.fetchContainers()
                    val zoneResult = repository.fetchZones()
                    val logResult = repository.fetchLogs()

                    val failures = mutableListOf<String>()
                    nodeResult.exceptionOrNull()?.let { failures += "nodes: ${it.message}" }
                    containerResult.exceptionOrNull()?.let { failures += "containers: ${it.message}" }
                    zoneResult.exceptionOrNull()?.let { failures += "zones: ${it.message}" }
                    logResult.exceptionOrNull()?.let { failures += "logs: ${it.message}" }

                    // Prefetch dns records for current zone
                    if (_selectedZoneId.value.isNotEmpty()) {
                        repository.fetchDnsRecords(_selectedZoneId.value)
                    }

                    // Prefetch Proxmox guests for any nodes of type PROXMOX
                    nodes.value.forEach { node ->
                        if (node.type == "PROXMOX") {
                            val res = repository.fetchProxmoxGuestsRemote(node.id)
                            res.onSuccess { guests ->
                                _nodeGuests.value = _nodeGuests.value + (node.id to guests)
                            }
                        }
                    }

                    if (failures.isNotEmpty() && !background) {
                        _errorMessage.value = failures.joinToString(" | ")
                    }
                }

                _lastSyncAt.value = syncTimeFormat.format(Date())
            } catch (e: Exception) {
                if (!background) {
                    _errorMessage.value = "Failed sync: ${e.message}"
                } else {
                    _healthError.value = e.message ?: "Background sync failed"
                }
            } finally {
                if (!background) {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun startBackgroundSync() {
        if (backgroundSyncJob != null) return
        backgroundSyncJob = viewModelScope.launch {
            while (isActive) {
                val canSyncFleetData = !token.value.isNullOrBlank() || demoMode.value
                _syncMode.value = if (canSyncFleetData) "polling" else "health-only"
                if (canSyncFleetData) {
                    refreshAll(background = true)
                } else {
                    val healthResult = repository.checkBackendHealth()
                    healthResult.onSuccess {
                        _backendHealth.value = it
                        _healthError.value = null
                    }.onFailure {
                        _healthError.value = it.message ?: "Backend health probe failed"
                    }
                    _lastSyncAt.value = syncTimeFormat.format(Date())
                }
                delay(30_000)
            }
        }
    }

    fun stopBackgroundSync() {
        backgroundSyncJob?.cancel()
        backgroundSyncJob = null
        _syncMode.value = "paused"
    }

    // Login Action
    fun login(usernameInput: String, passwordInput: String, roleInput: String, onNavigate: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.login(LoginRequest(usernameInput, passwordInput, roleInput))
            result.onSuccess {
                _toastMessage.value = "Authenticated successfully as '$roleInput'"
                refreshAll(forceFleetData = true)
                onNavigate()
            }.onFailure {
                _errorMessage.value = it.message ?: "Authentication failed."
            }
            _isLoading.value = false
        }
    }

    // Save Network Server Base URL configuration
    fun saveBaseUrl(url: String) {
        viewModelScope.launch {
            sessionManager.saveBaseUrl(url)
            _toastMessage.value = "API Base URL set: $url"
            // Re-sync
            refreshAll()
        }
    }

    // Toggle Demo Mode
    fun toggleDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.saveDemoMode(enabled)
            repository.resetSandboxData()
            _toastMessage.value = if (enabled) "Demo Mode Engine Enabled" else "Real Network API Enabled"
            refreshAll(forceFleetData = enabled)
        }
    }

    // Logout
    fun logout(onNavigate: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            repository.resetSandboxData()
            _toastMessage.value = "Administrative session purged."
            onNavigate()
        }
    }

    // Node Registrations (Admin only checking performed dynamically)
    fun registerNode(name: String, type: String, provider: String, ip: String, region: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.registerNode(name, type, provider, ip, region)
            res.onSuccess {
                _toastMessage.value = "Infrastructure Node '$name' registered successfully!"
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Registration fail: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Edit Node Config
    fun editNode(nodeId: String, name: String, ip: String, region: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.updateNode(nodeId, name, ip, region)
            res.onSuccess {
                _toastMessage.value = "Node '$name' updated."
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Update node fail: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Delete Node
    fun deleteNode(nodeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.deleteNode(nodeId)
            res.onSuccess {
                _toastMessage.value = "Infrastructure Node successfully deleted."
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Delete node fail: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Rotate Node token
    fun rotateNodeToken(nodeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.rotateNodeToken(nodeId)
            res.onSuccess {
                _toastMessage.value = "Secure agent token regenerated successfully!"
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Regenerate agent token failure: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Container status controller handles start, stop, restart
    fun controlContainer(containerId: String, action: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.controlContainer(containerId, action)
            res.onSuccess {
                _toastMessage.value = "Action '$action' dispatched to container!"
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Action fail: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Container auto heal toggles
    fun toggleContainerAutoHeal(containerId: String, enabled: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.toggleAutoHeal(containerId, enabled)
            res.onSuccess {
                _toastMessage.value = "Auto heal set to $enabled for container ID: $containerId"
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Auto-heal toggler fail: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Zone accessibility check
    fun checkZoneAccess(zoneId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.checkZoneAccess(zoneId)
            res.onSuccess {
                _toastMessage.value = "Access Result: $it"
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Cloudflare check access failed: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Add zone
    fun addCloudflareZone(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.addZoneLater(name)
            res.onSuccess {
                _toastMessage.value = "Cloudflare zone '$name' loaded static profile."
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Add Zone fail: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Create DNS Record
    fun createDnsRecord(zoneId: String, name: String, type: String, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.createDnsRecord(zoneId, name, type, content)
            res.onSuccess {
                _toastMessage.value = "Secure DNS Entry '$name' successfully provisioned."
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Create DNS record failure: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Delete DNS Record
    fun deleteDnsRecord(zoneId: String, recordId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.deleteDnsRecord(zoneId, recordId)
            res.onSuccess {
                _toastMessage.value = "DNS pointer deleted from Cloudflare server edge."
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Delete DNS record failure: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Emergency Lockdown Actions
    fun triggerLockdown() {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.triggerEmergencyKill()
            res.onSuccess {
                _toastMessage.value = "⚠️ INSTANT FORCE SHUTDOWN APPLIED."
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Lockdown command fail: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    fun releaseLockdown() {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.releaseEmergencyReset()
            res.onSuccess {
                _toastMessage.value = "Lockdown Released. Fleet agents restarted normally."
                refreshAll()
            }.onFailure {
                _errorMessage.value = "Release lockdown fail: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Proxmox guest control actions
    fun controlProxmoxGuest(nodeId: String, kind: String, vmid: Int, action: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.controlProxmoxGuest(nodeId, kind, vmid, action)
            res.onSuccess {
                _toastMessage.value = "PVE task '$action' completed successfully."
                refreshAll()
            }.onFailure {
                _errorMessage.value = "PVE task fail: ${it.message}"
            }
            _isLoading.value = false
        }
    }

    // Resolves and fetches actual live DNS records for a given domain/record-type over HTTP
    fun resolveLiveDns(domain: String, type: String) {
        _liveDnsQuery.value = domain
        _liveDnsType.value = type
        viewModelScope.launch {
            _liveDnsLoading.value = true
            _liveDnsError.value = null
            _liveDnsResults.value = emptyList()
            val result = repository.resolveLiveDns(domain, type)
            result.onSuccess {
                _liveDnsResults.value = it
                if (it.isEmpty()) {
                    _liveDnsError.value = "No answer records returned for $domain ($type)"
                }
            }.onFailure {
                _liveDnsError.value = it.message ?: "Query failed"
            }
            _liveDnsLoading.value = false
        }
    }

    fun clearLiveDnsResults() {
        _liveDnsResults.value = emptyList()
        _liveDnsError.value = null
        _liveDnsQuery.value = ""
    }

    override fun onCleared() {
        stopBackgroundSync()
        super.onCleared()
    }
}
