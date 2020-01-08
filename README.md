基本使用方法:
1.在peersim的Simulator.java中填入配置文件的路径然后执行
2.将peersim的Simulator.java的156行`Configuration.setConfig( new ParsedProperties(arg) );`中的arg改为args，然后打包用java命令传入配置文件路径

配置文件:
data中有两个配置文件
config.txt和configF.txt
config.txt是Gossip学习的配置文件，其中写有注释
configF.txt是联邦学习的配置文件，和前面一个差不多

由于我使用的是EDProtocol，所以感觉没有论文中的结果那么顺滑
但是如果用CDProtocol我又觉得不太真实
而且论文中也没说用的是哪个

我应该把没有用的东西都删了，有可能没删干净，还请见谅
代码很简单，一看就懂，只是写得太烂
还有就是我不小心删了一些文件，没办法恢复回去，所以可能会和我之前的代码有差距
如果有问题随时找我，我尽量负责到底

网上能找到的中文资料：
https://wenku.baidu.com/view/83b35f104431b90d6c85c7ae.html
