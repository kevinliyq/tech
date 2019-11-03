# Elastics Search

* elasticsearch
* plugin management
    * ik 中文分词 see more details at
    https://github.com/medcl/elasticsearch-analysis-ik
    
* elastics search 客户端连接方式
    * TransportClient TransportClient将会在ElasticSearch 7.0弃用并在8.0中完成删除
    * Rest Client 推荐使用
    * Jest Java社区开发的Java HTTP Rest客户端
    * Spring Data ElasticSearch是Spring集成ES的开发API

*  TransportClient
```java
pom dependency
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>transport</artifactId>
    <version>6.4.3</version>
</dependency>
<dependency>
     <groupId>org.elasticsearch</groupId>
     <artifactId>elasticsearch</artifactId>
     <version>6.4.3</version>
</dependency>
```

