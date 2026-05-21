package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String,
    val role: String // "admin" or "viewer"
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val username: String = "",
    val role: String = "viewer"
)

@JsonClass(generateAdapter = true)
data class Node(
    val id: String,
    val name: String,
    val type: String, // "PROXMOX", "DOCKER", "STANDALONE"
    val provider: String,
    val ip: String,
    val region: String,
    val status: String, // "online", "offline", "warning", "error"
    @Json(name = "last_ping") val lastPing: String,
    @Json(name = "agent_token") val agentToken: String = "",
    @Json(name = "install_command") val installCommand: String = ""
)

@JsonClass(generateAdapter = true)
data class CreateNodeRequest(
    val name: String,
    val type: String,
    val provider: String,
    val ip: String,
    val region: String
)

@JsonClass(generateAdapter = true)
data class Container(
    val id: String,
    val name: String,
    @Json(name = "node_id") val nodeId: String,
    @Json(name = "node_name") val nodeName: String,
    val image: String,
    val state: String, // "running", "stopped", "restarting"
    val ports: String,
    val domain: String,
    @Json(name = "auto_heal") val autoHeal: Boolean
)

@JsonClass(generateAdapter = true)
data class ContainerControlRequest(
    val action: String // "start", "stop", "restart"
)

@JsonClass(generateAdapter = true)
data class ContainerAutoHealRequest(
    val enabled: Boolean
)

@JsonClass(generateAdapter = true)
data class CloudflareZone(
    val id: String,
    val name: String,
    val status: String,
    @Json(name = "zone_health") val zoneHealth: String = "Good"
)

@JsonClass(generateAdapter = true)
data class DnsRecord(
    val id: String,
    @Json(name = "zone_id") val zoneId: String,
    val name: String,
    val type: String, // "A", "CNAME", "TXT", etc.
    val content: String,
    val ttl: Int = 1,
    val proxied: Boolean = true
)

@JsonClass(generateAdapter = true)
data class CreateDnsRecordRequest(
    @Json(name = "zone_id") val zoneId: String,
    val name: String,
    val type: String,
    val content: String,
    val ttl: Int = 1,
    val proxied: Boolean = true
)

@JsonClass(generateAdapter = true)
data class SystemLog(
    val id: String,
    val timestamp: String,
    val level: String, // "INFO", "WARN", "ERROR", "CRITICAL"
    val source: String,
    val message: String
)

@JsonClass(generateAdapter = true)
data class ProxmoxGuest(
    val vmid: Int,
    val name: String,
    val status: String, // "running", "stopped"
    val type: String, // "qemu", "lxc"
    val cpu: Double = 0.0,
    val mem: Long = 0L,
    val maxmem: Long = 0L
)

@JsonClass(generateAdapter = true)
data class NodeMetrics(
    val cpuUsage: Double,
    val memUsage: Double,
    val diskUsage: Double,
    val txBytes: Long,
    val rxBytes: Long,
    val uptimeSeconds: Long
)

@JsonClass(generateAdapter = true)
data class EmergencyState(
    @Json(name = "lockdown_active") val lockdownActive: Boolean
)

@JsonClass(generateAdapter = true)
data class LiveDnsRecord(
    val name: String,
    val type: String,
    val ttl: Int,
    val data: String
)

@JsonClass(generateAdapter = true)
data class BackendHealth(
    val status: String = "unknown",
    val version: String? = null,
    @Json(name = "supported_features") val supportedFeatures: List<String> = emptyList(),
    @Json(name = "websocket_enabled") val websocketEnabled: Boolean? = null,
    val message: String? = null
)
