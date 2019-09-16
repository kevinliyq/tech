Redis
#

1. Redis是一个开源的，基于内存的高效存储系统，可用作缓存，数据库，消息中间件。它支持五种数据结构
   * string
   * list
   * set
   * sorted_sort
   * hash
   提供bitmaps, hyperLogLogs等, 包含复制，LRU eviction, 事务和不同基本的持久化和高可用。
   
2. Redis 持久化
    * RDB (Redis Database)
   RDB 是 Redis 默认的持久化方案。在指定的时间间隔内，执行指定次数的写操作，则会将内存中的数据写入到磁盘中. 在指定的目录下生成了rdb文件。
   在不配置AOF的情况下Redis重启时会加载rdb文件恢复数据。
   
   RDB的触发有一下几种情况
      * 一定时间内发生指定次数的写操作
      * save阻塞操作或bgsave异步操作
      * flushall清空所有数据
      * shutdown 
       
   ```
    save 300 10
    ```
    在300秒之后至少有10个key发生变化则生成rdb文件。
    该方式的有点是启动时加载速度快，但是会存在key丢失的情况。如在dump之前发生断电，则这期间发生变化
    的key则会丢失
    
    当dump rdb时，Redis会调用操作系统fork出一个子进程，在该子进程中遍历整个hash表，利用copy on write，
    同时创建一个buffer用于存储在这期间发生变化的操作。
    把整个db dump下来。
   
   * AOF (Append Only File)
   AOF是为了解决rdb存在的较长时间数据更新丢失的情况而采用的类似mysql log-bin的方式，以日志的形式记录写操作。Redis重启是会根据这些命令逐条执行恢复数据。
   
   AOF默认是关闭的，需要设置'appendonly yes' 来激活
   日志的更新有几种方式：
   * always 每次的写操作都会触发更新，有点是数据完整性高但性能较差
   * no 不同步, 让操作系统来解决什么时候需要同步, 性能很快，但存在数据大量丢失的情况
   * everysec 默认方式，每秒钟同步一次，及时出现数据不一致也是在1秒之内
   
3. Redis的内存淘汰机制
   ```
   maxmemory 100mb
   maxmemory-policy allkeys-lru
   ```
   * 定期惰性删除
   Redis会隔一段时间世纪抽查一些key是否过期，但主要是通过在get key是判断key是否过来来删除。
   
   但是如果定期惰性删除也没办法删除key，如key没有过期时间则需要引入一下内存淘汰策略：
   * allkeys LRU
   
   noeviction:默认策略，不淘汰，如果内存已满，添加数据是报错。
   allkeys-lru:在所有键中，选取最近最少使用的数据抛弃。  推荐使用
   volatile-lru:在设置了过期时间的所有键中，选取最近最少使用的数据抛弃。
   allkeys-random: 在所有键中，随机抛弃。
   volatile-random: 在设置了过期时间的所有键，随机抛弃。
   volatile-ttl:在设置了过期时间的所有键，抛弃存活时间最短的数据。
  
   
   


   