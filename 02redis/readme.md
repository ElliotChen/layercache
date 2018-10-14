# Redis Cache 

## Check keys
```
keys *
```

result 

```
127.0.0.1:6379> keys *
1) "layer01::id1"
2) "layer100::error"
3) "layer02::id1"
```


## Check TTL

```
TTL layer01::id1
```

result

```
127.0.0.1:6379> TTL layer01::id1
(integer) 564
```
## Remove all keys

```
flushall
```