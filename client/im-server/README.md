#### 服务器接受的协议

1. 00 表示登陆
2. 01 表示请求私聊ip
3. 10 表示退出

#### 服务器发送协议
1. 00 表示找不到私聊对象
2. 01 表示私聊对象的地址
3. 10 表示用户表

#### 客户端发送协议
1. 00 登陆
2. 01 请求私聊
3. 10 退出
4. 11 广播信息

#### 客户端接受协议
1. 00 表示找不到私聊对象
2. 01 表示私聊对象的地址
3. 10 表示用户表
4. 11 广播信息