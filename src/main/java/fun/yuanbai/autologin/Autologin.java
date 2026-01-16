
package fun.yuanbai.autologin;

import fun.yuanbai.autologin.event.AutoLoginHandler;
import net.fabricmc.api.ModInitializer; // 导入Fabric API的模组初始化接口
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.apache.logging.log4j.LogManager; // 导入Log4j日志框架的日志管理器类
import org.apache.logging.log4j.Logger; // 导入Log4j日志框架的日志记录器类


// Autologin类，实现Fabric的ModInitializer接口，用于初始化模组
public class Autologin implements ModInitializer {

    // 定义一个静态的、最终的Logger对象，用于记录日志
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);



    // 当Fabric加载并初始化模组时，会调用此方法  
    @Override
    public void onInitialize() {

        //打印一条MOD初始化信息
        LOGGER.info("Autologin Initialized!");

            //客户端接收消息事件
            ClientReceiveMessageEvents.GAME.register(new AutoLoginHandler());

            //客户端发送消息事件
            ClientSendMessageEvents.COMMAND.register(new AutoLoginHandler());

            ClientPlayConnectionEvents.JOIN.register(new AutoLoginHandler());

    }
}