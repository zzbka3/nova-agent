#!/bin/bash
BASE="http://localhost:8080/v1/agent"

echo "============================================================================"
echo "  工作流全链路测试"
echo "============================================================================"

echo ""
echo "=== Step 1: 创建智能体（保存工作流） ==="
SAVE_RESULT=$(curl -s -X POST "$BASE/save" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Test Workflow Agent\",\"config\":$(cat test_workflow.json | python3 -c 'import sys,json; print(json.dumps(sys.stdin.read()))'),\"agentType\":0}")
echo "$SAVE_RESULT" | python3 -m json.tool

# 提取 appId
APP_ID=$(echo "$SAVE_RESULT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',''))" 2>/dev/null)
echo "获取到 appId: $APP_ID"

if [ -z "$APP_ID" ] || [ "$APP_ID" = "None" ]; then
  echo "创建失败，退出"
  exit 1
fi

echo ""
echo "=== Step 2: 验证工作流配置 ==="
curl -s -X POST "$BASE/validate" \
  -H "Content-Type: application/json" \
  -d "{\"appId\":\"$APP_ID\",\"config\":$(cat test_workflow.json | python3 -c 'import sys,json; print(json.dumps(sys.stdin.read()))')}" \
  | python3 -m json.tool

echo ""
echo "=== Step 3: 生成会话 ID ==="
CONV_ID=$(curl -s "$BASE/conversation?appId=$APP_ID" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',''))" 2>/dev/null)
echo "获取到 conversationId: $CONV_ID"

echo ""
echo "=== Step 4: 执行工作流（talk） ==="
echo "发送请求..."
TALK_RESULT=$(curl -s -X POST "$BASE/talk" \
  -H "Content-Type: application/json" \
  -d "{\"app_id\":\"$APP_ID\",\"conversation_id\":\"$CONV_ID\",\"inputs\":{\"query\":\"你好\"},\"query\":\"你好\",\"debug\":0}")
echo "$TALK_RESULT" | python3 -m json.tool

echo ""
echo "=== Step 5: 查询执行记录 ==="
curl -s -X POST "$BASE/runningInfo" \
  -H "Content-Type: application/json" \
  -d "{\"app_id\":\"$APP_ID\",\"conversation_id\":\"$CONV_ID\",\"timestamp\":0}" \
  | python3 -m json.tool

echo ""
echo "============================================================================"
echo "  测试完成"
echo "============================================================================"
