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
   AOF记录Redis写命令包含append, write and sync. 
     - append: append to aof_buf缓冲区
     - write and sync: 涉及到一下策略
   
   AOF默认是关闭的，需要设置'appendonly yes' 来激活
   * always 每个写命令同步写入磁盘，把aof_buf缓冲区的内容写入并同步到AOF文件，优点是数据完整性高但性能较差
   * no 不同步, 让操作系统来解决什么时候需要同步, 性能很快，但存在数据大量丢失的情况
   * everysec 默认方式，把aof区的内容写入AOF文件，每秒钟同步一次，及时出现数据不一致也是在1秒之内
   
   为了提高写效率，操作系统在用户调用write函数写文件时，会暂存在内存缓冲区，直到缓冲区写满或是超过时间，才会把缓冲区的数据写入磁盘.
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
  
4. AOF 重写
   随着写操作的增多，AOF文件会变得越来越大，过大的AOF文件可能对Redis服务有影响，使用AOF还原数据时间也会过长。
   AOF重写的目的就是减少AOF文件的大小。AOF文件重写不会对现有的AOF文件进行如何读取和写入操作，而是读取服务器当前的存储状态来实现.

   可手动或自动触发Redis AOF重写
   bgrewriteof
   
   auto-aof-rewrite-percentage
   auto-aof-rewrite-min-size
   表示AOF的文件大于最小体积并且比原来的体检增加多少时自动触发AOF重写
   
   


   
