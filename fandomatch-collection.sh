#!/bin/bash
DIR="FandoMatch-API"
mkdir -p "$DIR" && cd "$DIR" || exit

# 1. Манифест коллекции
cat > bruno.json << 'EOF'
{
  "version": "1",
  "name": "FandoMatch-API",
  "type": "collection",
  "ignore": ["node_modules", ".git"]
}
EOF

# Вспомогательная функция генерации .bru
gen_bru() {
  local id="$1" name="$2" method="$3" url="$4" auth="$5" body="$6" tests="$7"
  local method_lower=$(echo "$method" | tr '[:upper:]' '[:lower:]')
  local filename="${name// /_}.bru"

  # Заголовки
  local hdr="Content-Type: application/json"
  [[ "$auth" == "bearer" ]] && hdr="Authorization: {{auth_token}}\n  Content-Type: application/json"
  [[ "$auth" == "apikey" ]] && hdr="X-API-Key: b2a43cf3-fe80-4575-9b1c-8971a5a50359\n  Content-Type: application/json"

  # Тесты
  local test_blk=""
  [[ -n "$tests" ]] && test_blk=$'tests {\n'"$tests"$'\n}'

  cat > "$filename" << BRU_EOF
meta {
  id: ${id}
  name: ${name}
  type: http
  method: ${method}
}

${method_lower} {
  url: http://localhost:8080${url}
}

headers {
$(echo -e "$hdr")
}

body:json {
  ${body}
}
${test_blk}
BRU_EOF
}

# ================= USERS SERVICE =================
gen_bru "auth-register" "Auth Register" "POST" "/auth/register" "" \
'{"email":"test@mail.com","username":"testuser","birth_date":946684800,"name":"Test","hashed_password":"$2a$10$..."}' \
'const r=pm.response.json(); if(r.successResponse?.access_token){let t=r.successResponse.access_token; bru.setVar("auth_token",t.startsWith("Bearer ")?t:"Bearer "+t); console.log("✅ Token saved");}'

gen_bru "auth-login" "Auth Login" "POST" "/auth/login" "" \
'{"username":"testuser","hashed_password":"$2a$10$..."}' \
'const r=pm.response.json(); if(r.successResponse?.access_token){let t=r.successResponse.access_token; bru.setVar("auth_token",t.startsWith("Bearer ")?t:"Bearer "+t); console.log("✅ Token saved");}'

gen_bru "auth-change-password" "Change Password" "POST" "/auth/change-password" "bearer" \
'{"old_password":"old_pass","new_password":"new_pass"}'

gen_bru "auth-logout" "Logout" "POST" "/auth/logout" "bearer" '{}'

gen_bru "users-get-credentials" "Get User Credentials" "GET" "/users/get-user-credentials" "bearer" ''

gen_bru "token-public-jwt" "Get Public JWT Key" "GET" "/token/public-jwt" "" ''

gen_bru "token-refresh" "Refresh Token" "POST" "/token/refresh" "" \
'{"refresh_token":"your_refresh_token_here"}'

gen_bru "users-device-token" "Save FCM Token" "PUT" "/users/device-token" "bearer" \
'{"fcm_token":"firebase_device_token"}'

gen_bru "users-internal-device-token" "Get FCM (Internal)" "POST" "/users/internal/device-token" "apikey" \
'{"user_id":"550e8400-e29b-41d4-a716-446655440000"}'

gen_bru "users-get-by-id" "Get User By ID" "POST" "/users/get-by-id" "bearer" \
'{"user_id":"550e8400-e29b-41d4-a716-446655440000"}'

# ================= CORE SERVICE =================
gen_bru "core-get-profile" "Get Profile" "POST" "/core/user/profile" "bearer" \
'{"uuid":"550e8400-e29b-41d4-a716-446655440000"}'

gen_bru "core-edit-profile" "Edit Profile" "PATCH" "/core/user/profile/edit" "bearer" \
'{"bio":"Updated bio","name":"NewName","gender":"OTHER","city":{"name_en":"Moscow","name_ru":"Москва"}}'

gen_bru "core-get-friends" "Get Friends" "PATCH" "/core/user/profile/friends" "bearer" \
'{"uuid":"550e8400-e29b-41d4-a716-446655440000"}'

gen_bru "core-get-pending" "Get Pending Requests" "PATCH" "/core/user/profile/pending_requests" "bearer" \
'{"uuid":"550e8400-e29b-41d4-a716-446655440000"}'

gen_bru "core-match-next" "Get Next Candidates" "POST" "/core/match/next" "bearer" \
'{"batch_size":10}'

gen_bru "core-match-react" "React Like/Dislike" "POST" "/core/match/react" "bearer" \
'{"target_uuid":"550e8400-e29b-41d4-a716-446655440000","action":"LIKE"}'

gen_bru "core-match-filter" "Set Filters" "POST" "/core/match/filter" "bearer" \
'{"filters":{"gender":["FEMALE"],"age_from":18,"age_to":35,"only_in_user_city":false,"fandom_category":["ANIME_MANGA"]}}'

gen_bru "core-get-filters" "Get Current Filters" "GET" "/core/match/get_current_filters" "bearer" ''

gen_bru "core-posts-get" "Get Posts" "POST" "/core/posts/get" "" \
'{"uuid":"550e8400-e29b-41d4-a716-446655440000","pagination":{"cursor_timestamp":0,"size":20}}'

gen_bru "core-posts-create" "Create Post" "POST" "/core/posts/create" "bearer" \
'{"title":"My Post","content":"Hello world","fandom_id":"fandom-uuid","media_items":[{"media_id":"media-uuid","media_type":"IMAGE"}]}'

gen_bru "core-post-single" "Get Single Post" "GET" "/core/posts/550e8400-e29b-41d4-a716-446655440001" "" ''

gen_bru "core-post-comments" "Get Post Comments" "POST" "/core/posts/550e8400-e29b-41d4-a716-446655440001/comments" "" \
'{"cursor_timestamp":0,"size":20}'

gen_bru "core-post-like" "Like Post" "POST" "/core/posts/550e8400-e29b-41d4-a716-446655440001/like" "bearer" ''

gen_bru "core-feed" "Get Match Feed" "POST" "/core/feed" "bearer" \
'{"uuid":"550e8400-e29b-41d4-a716-446655440000","pagination":{"cursor_timestamp":0,"size":15}}'

gen_bru "core-fandoms-user" "Get User Fandoms" "POST" "/core/fandoms/user" "" \
'{"uuid":"550e8400-e29b-41d4-a716-446655440000"}'

gen_bru "core-fandoms-categories" "Get Fandom Categories" "GET" "/core/fandoms/categories" "" ''

gen_bru "core-fandoms-request-new" "Request New Fandom" "POST" "/core/fandoms/request-new" "" \
'{"name":"New Fandom","description":"Desc","category":"GAMES","author_uuid":"550e8400-e29b-41d4-a716-446655440000"}'

# ================= MESSAGING SERVICE =================
gen_bru "msg-presigned-upload" "Get Presigned Upload URL" "POST" "/messaging/media/presigned-upload" "bearer" \
'{"media_type":"IMAGE"}'

gen_bru "msg-chat-previews" "Get Chat Previews" "POST" "/messaging/chats/previews" "bearer" \
'{"size":20}'

gen_bru "msg-chat-info" "Get Chat Info" "GET" "/messaging/chats/550e8400-e29b-41d4-a716-446655440000" "bearer" ''

gen_bru "msg-chat-messages" "Get Chat Messages" "POST" "/messaging/chats/550e8400-e29b-41d4-a716-446655440000/messages" "bearer" \
'{"chat_id":"660e8400-e29b-41d4-a716-446655440000","size":50}'

gen_bru "msg-send-message" "Send Message" "POST" "/messaging/chats/550e8400-e29b-41d4-a716-446655440000/send" "bearer" \
'{"content":"Привет!","timestamp":'$(date +%s%3N)',"media_ids":[]}'

echo "✅ Готово! Коллекция создана в папке: ./FandoMatch-API"
echo "📂 Откройте её в Bruno: File → Open Collection → выберите папку FandoMatch-API"
echo "🔑 После отправки Login/Register токен сохранится автоматически."