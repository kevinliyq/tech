# Elastics Search

* elasticsearch
elasticsearch是一个开源的，分布式的基于Lucene的全文搜索引擎，它可以做到
  * 一个分布式的实时文档存储，每个字段都可以做到被索引和搜索
  * 倒序索引
  * 实时分析搜索引擎
  * 支持大规模的扩展，支持PB级的结构化和非结构化数据的存储
  * 提供多种访问模式，如基于HTTP的Restful方式
  
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

