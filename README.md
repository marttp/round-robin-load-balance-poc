# POC - HTTP Round Robin API

## Simple API

### curl command

```bash
curl -X POST http://localhost:8080/api/v1/echo \
  -H "Content-Type: application/json" \
  -d '{"game":"Mobile Legends", "gamerID":"GYUTDTE", "points":20}'
```