
Memcached

1. start memcached by docker
* docker run --name memcached2 -m 128m -p 11212:11211 -d memcached

2. telnet to memcached
* telnet host port
* <command name> <key> <flags> <exptime> <bytes>
  value

3. memcached
* memcached 特点
  * 内存存储方式
  * memcached不互相通信的分布式
  * 基于libevent的多线程事件处理
  * hash算法
  memcached的node选择取决于client的实现， 通常使用一致性hash的方式，节点通过hash后分布在一个0 - 2^32 -1的环上。如Ketama采用一致性加上虚拟节点的方式hash->node, 使得每个节点尽可能均匀分布在环上。
  这样及时增加或者是减少其中的node，受影响的key只有1/n。
  
* Memcached是多线程，非阻塞IO复用的网络模型.
分为监听主线程和worker子线程，监听线程监听网络连接，接受请求后，将连接描述字pipe 传递给worker线程，进行读写IO, 网络层使用libevent封装的事件库，多线程模型可以发挥多核作用，但是引入了cache coherency和锁的问题，比如，Memcached最常用的stats 命令，实际Memcached所有操作都要对这个全局变量加锁，进行计数等工作，带来了性能损耗。

* 内存
Memcached使用预分配的内存池的方式，使用slab和大小不同的chunk来管理内存，Item根据大小选择合适的chunk存储，内存池的方式可以省去申请/释放内存的开销，并且能减小内存碎片产生，但这种方式也会带来一定程度上的空间浪费，并且在内存仍然有很大空间时，新的数据也可能会被剔除
Redis使用现场申请内存的方式来存储数据，并且很少使用free-list等方式来优化内存分配，会在一定程度上存在内存碎片，Redis跟据存储命令参数，会把带过期时间的数据单独存放在一起，并把它们称为临时数据，非临时数据是永远不会被剔除的，即便物理内存不够

* 数据一致性
memcached采用cas方式保证多线程处理下数据一致性，redis采用multi事务的方式保证操作原子性





