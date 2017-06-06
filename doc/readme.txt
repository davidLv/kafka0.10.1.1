中文文档：
http://m.blog.csdn.net/article/details?id=52210826

两种类型：
Kafka Stream定义了两种基本抽象：KStream 和 KTable，区别来自于key-value对值如何被解释，在一个流中(KStream)，每个key-value是一个独立的信息片断，比如，用户购买流是：alice->黄油，bob->面包，alice->奶酪面包，我们知道alice既买了黄油，又买了奶酪面包。
另一方面，对于一个表table( KTable)，是代表一个变化日志，如果表包含两对同样key的key-value值，后者会覆盖前面的记录，因为key值一样的，比如用户地址表：alice -> 纽约, bob -> 旧金山, alice -> 芝加哥，意味着Alice从纽约迁移到芝加哥，而不是同时居住在两个地方。
这两个概念之间有一个二元性，一个流能被看成表，而一个表也可以看成流。