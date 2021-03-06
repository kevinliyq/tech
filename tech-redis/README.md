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
   
5. Sharding
   * Server Sharding
   Redis官方集群方案Redis Cluster是服务器端Sharding的一种方式, 
   Redis Cluster中，Sharding采用slot(槽)的概念，一共分成16384个槽，这有点儿类似前面讲的pre sharding思路。对于每个进入Redis的键值对，根据key进行散列，分配到这16384个slot中的某一个中。使用的hash算法也比较简单，就是CRC16后16384取模。Redis集群中的每个node(节点)负责分摊这16384个slot中的一部分，也就是说，每个slot都对应一个node负责处理。当动态添加或减少node节点时，需要将16384个槽做个再分配，槽中的键值也要迁移。当然，这一过程，在目前实现中，还处于半自动状态，需要人工介入。Redis集群，要保证16384个槽对应的node都正常工作，如果某个node发生故障，那它负责的slots也就失效，整个集群将不能工作。为了增加集群的可访问性，官方推荐的方案是将node配置成主从结构，即一个master主节点，挂n个slave从节点。这时，如果主节点失效，Redis Cluster会根据选举算法从slave节点中选择一个上升为主节点，整个集群继续对外提供服务。这非常类似前篇文章提到的Redis Sharding场景下服务器节点通过Sentinel监控架构成主从结构，只是Redis Cluster本身提供了故障转移容错的能力。
   Redis Cluster的新节点识别能力、故障判断及故障转移能力是通过集群中的每个node都在和其它nodes进行通信，这被称为集群总线(cluster bus)。它们使用特殊的端口号，即对外服务端口号加10000。例如如果某个node的端口号是6379，那么它与其它nodes通信的端口号是16379。nodes之间的通信采用特殊的二进制协议。
   对客户端来说，整个cluster被看做是一个整体，客户端可以连接任意一个node进行操作，就像操作单一Redis实例一样，当客户端操作的key没有分配到该node上时，就像操作单一Redis实例一样，当客户端操作的key没有分配到该node上时，Redis会返回转向指令，指向正确的node，这有点儿像浏览器页面的302 redirect跳转。
   
   为什么槽是16384， 因为在Redis集群中会节点之间会发送ping，pong消息来识别节点的存活。在1000个node的情况下，传输消息的大小是2M，而1000个节点的node基本满足绝大部分场景的需要。
   * Client Sharding
   其主要思想是采用哈希算法将Redis数据的key进行散列，通过hash函数，特定的key会映射到特定的Redis节点上。这样，客户端就知道该向哪个Redis节点操作数据。
   
   Jedis支持Redis Sharding功能：
   * 采用一致性哈希算法(consistent hashing)，将key和节点name同时hashing，然后进行映射匹配，采用的算法是MURMUR_HASH。采用一致性哈希而不是采用简单类似哈希求模映射的主要原因是当增加或减少节点时，不会产生由于重新匹配造成的rehashing。一致性哈希只影响相邻节点key分配，影响量小。
   * 为了避免一致性哈希只影响相邻节点造成节点分配压力，ShardedJedis会对每个Redis节点根据名字(没有，Jedis会赋予缺省名字)会虚拟化出160个虚拟节点进行散列。根据权重weight，也可虚拟化出160倍数的虚拟节点。用虚拟节点做映射匹配，可以在增加或减少Redis节点时，key在各Redis节点移动再分配更均匀，而不是只有相邻节点受影响
   
   Redis Sharding采用客户端Sharding方式，服务端Redis还是一个个相对独立的Redis实例节点，没有做任何变动。同时，我们也不需要增加额外的中间处理组件，这是一种非常轻量、灵活的Redis多实例集群方法。
   当然，Redis Sharding这种轻量灵活方式必然在集群其它能力方面做出妥协。比如扩容，当想要增加Redis节点时，尽管采用一致性哈希，毕竟还是会有key匹配不到而丢失，这时需要键值迁移。
   作为轻量级客户端sharding，处理Redis键值迁移是不现实的，这就要求应用层面允许Redis中数据丢失或从后端数据库重新加载数据。但有些时候，击穿缓存层，直接访问数据库层，会对系统访问造成很大压力。有没有其它手段改善这种情况？
   Redis作者给出了一个比较讨巧的办法–presharding，即预先根据系统规模尽量部署好多个Redis实例，这些实例占用系统资源很小，一台物理机可部署多个，让他们都参与sharding，当需要扩容时，选中一个实例作为主节点，新加入的Redis节点作为从节点进行数据复制。数据同步后，修改sharding配置，让指向原实例的Shard指向新机器上扩容后的Redis节点，同时调整新Redis节点为主节点，原实例可不再使用。
   这样，我们的架构模式变成一个Redis节点切片包含一个主Redis和一个备Redis。在主Redis宕机时，备Redis接管过来，上升为主Redis，继续提供服务。主备共同组成一个Redis节点，通过自动故障转移，保证了节点的高可用性。
   
   * 代理中间件实现Redis集群
   服务端sharding的Redis Cluster其优势在于服务端Redis集群拓扑结构变化时，客户端不需要感知，客户端像使用单Redis服务器一样使用Redis集群，运维管理也比较方便。
   客户端sharding技术其优势在于服务端的Redis实例彼此独立，相互无关联，每个Redis实例像单服务器一样运行，但是服务器端扩容时客户端也要变动。
   
   代理中间件就是在这种情况下引入的。
   twemproxy处于客户端和服务器的中间，将客户端发来的请求，进行一定的处理后(如sharding)，再转发给后端真正的Redis服务器。也就是说，客户端不直接访问Redis服务器，而是通过twemproxy代理中间件间接访问。
   
5. 主从复制
   从服务器会向主服务器发出SYNC指令，当主服务器接到此命令后，就会调用BGSAVE指令来创建一个子进程专门进行数据持久化工作，也就是将主服务器的数据写入RDB文件中。在数据持久化期间，主服务器将执行的写指令都缓存在内存中。
   
   在BGSAVE指令执行完成后，主服务器会将持久化好的RDB文件发送给从服务器，从服务器接到此文件后会将其存储到磁盘上，然后再将其读取到内存中。这个动作完成后，主服务器会将这段时间缓存的写指令再以redis协议的格式发送给从服务器。
   
   另外，要说的一点是，即使有多个从服务器同时发来SYNC指令，主服务器也只会执行一次BGSAVE，然后把持久化好的RDB文件发给多个下游。在redis2.8版本之前，如果从服务器与主服务器因某些原因断开连接的话，都会进行一次主从之间的全量的数据同步；而在2.8版本之后，redis支持了效率更高的增量同步策略，这大大降低了连接断开的恢复成本。
   
   主服务器会在内存中维护一个缓冲区，缓冲区中存储着将要发给从服务器的内容。从服务器在与主服务器出现网络瞬断之后，从服务器会尝试再次与主服务器连接，一旦连接成功，从服务器就会把“希望同步的主服务器ID”和“希望请求的数据的偏移位置（replication offset）”发送出去。主服务器接收到这样的同步请求后，首先会验证主服务器ID是否和自己的ID匹配，其次会检查“请求的偏移位置”是否存在于自己的缓冲区中，如果两者都满足的话，主服务器就会向从服务器发送增量内容。
   
   引用：【https://www.cnblogs.com/lukexwang/p/4711977.html】 

6. 分布式锁
   当我们的服务处在不同的分布式的情况下，要操作临界区的内容，我们需要分布式锁，而实现分布式锁可以通过数据库, Redis或者是zookeepr来创建。
   Redis是单线程处理和响应用户的请求，对数据操作是线程安全的，除此之外我们还要考虑：
   * 锁的有效期；当原因客户端断电等问题，如果锁没有设置有效期，那锁可能永远不能释放
   * 上锁需要原子操作; 如果通过set和expire两个操作上锁，这分别是两个操作，没有做到原子性就可能出现#1中问题
   * 锁最好的情况由拥有者来释放，除非在拥护者无效的情况等待过期
   
7. Sentinel 哨兵
   参看: 【https://redis.io/topics/sentinel】
   Sentinel是用于监控Redis Master/Slave的一种解决方案，它可以实现在Redis Master下线的情况进行主从切换。
   同时Sentinel可以搭建HA的配置，Sentinel的配置：
   
   '''
   sentinel monitor mymaster 127.0.0.1 6379 2
   sentinel down-after-milliseconds mymaster 60000
   sentinel failover-timeout mymaster 180000
   sentinel parallel-syncs mymaster 1
   
   sentinel monitor resque 192.168.1.3 6380 4
   sentinel down-after-milliseconds resque 10000
   sentinel failover-timeout resque 180000
   sentinel parallel-syncs resque 5
   
   ##### enable this if no password
   bind 0.0.0.0 if enable remote access
   protected-mode no
   
   '''
   
   
   Sentinel的监控有以下特点：
   
   * Sentinels与其他Sentinels保持联系，以便相互检查彼此的可用性，并交换消息。但是，您不需要在运行的每个Sentinel实例中配置其他Sentinel地址的列表，因为Sentinel使用Redis实例发布/订阅功能来发现监视相同主服务器和从服务器的其他Sentinel。
   通过将hello消息发送到名为的通道 来实现此功能__sentinel__:hello。
   同样，您不需要配置连接到主服务器的从服务器的列表是什么，因为Sentinel会在查询Redis时自动发现此列表。vdsSSFFFFFFFFFFSSSSVJYGYHY66IH6IGDGJOITJHKG
   
   * 每个Sentinel __sentinel__:hello每两秒钟将消息发布到每个受监视的主从属Pub / Sub通道，并通过ip，port，runid宣布其存在。
   Hello消息还包括主服务器的完整当前配置。如果接收Sentinel具有给定主机的配置，该配置早于接收的信息，则会立即更新为新配置。
   在向主服务器添加新的哨兵之前，Sentinel始终检查是否已存在具有相同runid或相同地址（ip和端口对）的标记。在这种情况下，将删除所有匹配的标记，并添加新的标记。
   
   * Sentinel 采用Raft协议来选举leader。
     * Raft协议的每个副本都会处于三种状态之一：Leader、Follower、Candidate。当Follower在一定时间内没有收到leader的心跳时会触发leader的选举，选举是基于term(任期)，term有唯一的id保存在每个node上。每个term一开始就进行选主
     Follower将自己维护的current_term_id加1。
     然后将自己的状态转成Candidate
     发送RequestVoteRPC消息(带上current_term_id) 给 其它所有server
     
     * 自己被选成了主。当收到了majority的投票后，状态切成Leader，并且定期给其它的所有server发心跳消息（不带log的AppendEntriesRPC）以告诉对方自己是current_term_id所标识的term的leader。每个term最多只有一个leader，term id作为logical clock，在每个RPC消息中都会带上，用于检测过期的消息。当一个server收到的RPC消息中的rpc_term_id比本地的current_term_id更大时，就更新current_term_id为rpc_term_id，并且如果当前state为leader或者candidate时，将自己的状态切成follower。如果rpc_term_id比本地的current_term_id更小，则拒绝这个RPC消息。
     * 别人成为了主。当Candidator在等待投票的过程中，收到了大于或者等于本地的current_term_id的声明对方是leader的AppendEntriesRPC时，则将自己的state切成follower，并且更新本地的current_term_id。
     * 没有选出主。当投票被瓜分，没有任何一个candidate收到了majority的vote时，没有leader被选出。这种情况下，每个candidate等待的投票的过程就超时了，接着candidates都会将本地的current_term_id再加1，发起RequestVoteRPC进行新一轮的leader election

   
8. Reids Cluster
   Redis Cluster是Redis提供HA的一种服务器端实现方案。Redis集群包括若干个master node，可以相应write操作。每个master node可以配置多个从slaves。
   
   Redis Cluster没有使用一致性hash, 而是引入了Slot哈希槽的概念.
   Redis Cluster有16384个哈希槽，这些槽会分配给对应的master nodes，对于每个KeyRedis通过CRC16计算得出slot，从而得到对应存储改key的node。
   客户端可以保存slot和node的mapping，当客户端发送的key不在对应的node，该node会返回MOVED相应，从而把客户端重定向到key所在的node上。
   
   Redis Cluster不能保证数据强一致性，因为Redis cluster收到写操作并在master完成写操作之后，会首先返回ack给客户端，之后才会在master和slaves之间做异步复制。
   所以当write写操作及ack完成但复制前master出现对大部分nodes不可达，这是大部分master会选举出新的slave来承担master的，那么是会存在数据丢失的可能。
   
   可以通过一下命令创建一个redis cluster
   redis-cluster start
   redis-cluster create
   
   


   
