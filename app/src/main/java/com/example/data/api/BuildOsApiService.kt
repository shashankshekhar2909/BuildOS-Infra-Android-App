package com.example.data.api

import com.example.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface BuildOsApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/infra/nodes")
    suspend fun getNodes(
        @Header("Authorization") token: String
    ): Response<List<Node>>

    @POST("api/infra/nodes/register")
    suspend fun registerNode(
        @Header("Authorization") token: String,
        @Body request: CreateNodeRequest
    ): Response<Node>

    @PATCH("api/infra/nodes/{id}")
    suspend fun updateNode(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<Node>

    @DELETE("api/infra/nodes/{id}")
    suspend fun deleteNode(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @POST("api/infra/nodes/{id}/regenerate-token")
    suspend fun regenerateNodeToken(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Node>

    @GET("api/infra/containers")
    suspend fun getContainers(
        @Header("Authorization") token: String
    ): Response<List<Container>>

    @POST("api/infra/containers/{id}/control")
    suspend fun controlContainer(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: ContainerControlRequest
    ): Response<Unit>

    @POST("api/infra/containers/{id}/auto-heal")
    suspend fun setContainerAutoHeal(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: ContainerAutoHealRequest
    ): Response<Unit>

    @GET("api/infra/logs")
    suspend fun getLogs(
        @Header("Authorization") token: String
    ): Response<List<SystemLog>>

    @GET("api/cloudflare/zones")
    suspend fun getCloudflareZones(
        @Header("Authorization") token: String
    ): Response<List<CloudflareZone>>

    @POST("api/cloudflare/zones")
    suspend fun createZone(
        @Header("Authorization") token: String,
        @Body zone: Map<String, String>
    ): Response<CloudflareZone>

    @GET("api/cloudflare/dns")
    suspend fun getDnsRecords(
        @Header("Authorization") token: String,
        @Query("zone_id") zoneId: String
    ): Response<List<DnsRecord>>

    @POST("api/cloudflare/dns")
    suspend fun createDnsRecord(
        @Header("Authorization") token: String,
        @Body request: CreateDnsRecordRequest
    ): Response<DnsRecord>

    @DELETE("api/cloudflare/dns/{id}")
    suspend fun deleteDnsRecord(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @GET("api/cloudflare/zones/{id}/check")
    suspend fun checkZoneAccess(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Map<String, Any>>

    @POST("api/infra/emergency-kill")
    suspend fun emergencyKill(
        @Header("Authorization") token: String
    ): Response<EmergencyState>

    @POST("api/infra/emergency-reset")
    suspend fun emergencyReset(
        @Header("Authorization") token: String
    ): Response<EmergencyState>

    @GET("api/infra/nodes/{id}/metrics")
    suspend fun getNodeMetrics(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<NodeMetrics>

    @GET("api/infra/nodes/{id}/pve-guests")
    suspend fun getProxmoxGuests(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<List<ProxmoxGuest>>

    @POST("api/infra/nodes/{id}/pve-guests/{kind}/{vmid}/control")
    suspend fun controlProxmoxGuest(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Path("kind") kind: String, // "qemu" or "lxc"
        @Path("vmid") vmid: Int,
        @Body request: Map<String, String> // e.g. "action" -> "start" / "stop" / "shutdown"
    ): Response<Unit>
}
