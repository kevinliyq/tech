tech-zookeeper

1. What is zookeeper
* zookeeper是一个开源，可支持服务同步，配置维护，命名服务的分布式协调系统。简单来说zooker提供了
  * 一个共享的命名空间类似Linux的文件系统。节点称为znode，不同于文件服务器的是znode可以存储数据
  * 通知机制。当节点的状态发生变化，如果数据改变，删除或者是子节点的增加和删除都会触发通知
  * 一个提供数据冗余，高可用，有序的分布式系统; zookeeper一般有2n+1个zookeeper实例组成，只要n+1的实例存活就可以继续提供服务。
  
2. znode的类型
  * PERSISTENT - 持久化的目录节点
  * PERSISTENT_SEQUENTIAL - 持久化顺序目录节点
  * EPHEMERAL - 临时目录节点
  * EPHEMERAL_SEQUENTIAL -临时目录顺序节点
  
3. znode版本
znode包含3个版本：
* dataVersion 每次set数据时版本会增加，无论数据是否有变化
* cVersion，子节点变化时，该版本会增加
* aclVersion 



