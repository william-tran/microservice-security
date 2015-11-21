#!/bin/bash
set -e
set -x
# vars to define
# UAA_ENDPOINT eg uaa.systemdomain.com
# ADMIN_CLIENT_ID eg admin
# ADMIN_CLIENT_SECRET get this from ops manager
# IDENTITY_ZONE_ID this is the guid of the identity zone, it’s the first guid in the URI for any page in the Pivotal SSO UI 
# ZONEADMIN_CLIENT_ID pick a name for the admin client in the zone
# ZONEADMIN_CLIENT_SECRET 

uaac target $UAA_ENDPOINT --skip-ssl-validation
uaac token client get $ADMIN_CLIENT_ID -s $ADMIN_CLIENT_SECRET
uaac client add temp --authorities zones.write,scim.zones --scope zones.$IDENTITY_ZONE_ID.admin --authorized_grant_types client_credentials,password -s $ZONEADMIN_CLIENT_SECRET
uaac user add temp --email cf-spring-cloud-services@pivotal.io -p $ZONEADMIN_CLIENT_SECRET
uaac member add zones.$IDENTITY_ZONE_ID.admin temp
uaac token owner get temp temp -p $ZONEADMIN_CLIENT_SECRET -s $ZONEADMIN_CLIENT_SECRET
uaac curl /oauth/clients -k -H "Content-type:application/json" -H "X-Identity-Zone-Id:$IDENTITY_ZONE_ID" -X POST -d "{\"client_id\":\"$ZONEADMIN_CLIENT_ID\",\"client_secret\":\"$ZONEADMIN_CLIENT_SECRET\",\"scope\":[\"uaa.none\"],\"resource_ids\":[\"none\"],\"authorities\":[\"uaa.admin\",\"clients.read\",\"clients.write\",\"scim.read\",\"scim.write\",\"clients.secret\"],\"authorized_grant_types\":[\"client_credentials\"]}"
uaac token client get $ADMIN_CLIENT_ID -s $ADMIN_CLIENT_SECRET
uaac user delete temp
uaac client delete temp
