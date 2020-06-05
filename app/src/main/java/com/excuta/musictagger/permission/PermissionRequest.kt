package com.excuta.musictagger.permission


sealed class PermissionRequest(val permission: String?)

class Granted(permissionRequest: String) : PermissionRequest(permissionRequest)
class Denied(permissionRequest: String) : PermissionRequest(permissionRequest)
/**
 * Rare case, usually permission will be either granted or denied
 **/
class Cancelled : PermissionRequest(null)