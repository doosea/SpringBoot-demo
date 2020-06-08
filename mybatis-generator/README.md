# mybatis-generator 逆向工程的使用

## 配置流程

1. 首先创建 Springboot 工程， 引入 `lombok`、`web` 、`mybatis` 、`mysql`依赖；

2. 添加 `mybatis-generator-maven-plugin`插件
    ```xml
   <plugin>
       <groupId>org.mybatis.generator</groupId>
       <artifactId>mybatis-generator-maven-plugin</artifactId>
       <version>1.4.0</version>
       <configuration>
           <verbose>true</verbose>
           <overwrite>true</overwrite>
       </configuration>
   </plugin>
    ```
  
3. 配置文件 `application.yml`
    ```xml
    server:
      port: 8080 #服务端口
    
    spring:
      datasource:
        driver-class-name: com.mysql.cj.jdbc.Driver  #数据库驱动包
        url: jdbc:mysql://localhost:3306/cloud?characterEncoding=utf8&useSSL=false&serverTimezone=UTC
        password: root
        username: root
    
    mybatis:
      mapper-locations: classpath:mapper/*.xml
      type-aliases-package: cn.enn.mybatisgenerator.model        #所有model别名所在包
      configuration:
        map-underscore-to-camel-case: true      # 开启驼峰映射
    ```

4. 配置文件`generatorConfig.xml` (在resources目录下)
    
    需要修改的参数：
    * 本地数据库驱动程序jar包的全路径  使用时改称自己的本地路径
    * 数据库的相关配置
    * 实体类生成的位置
    * Mapper.xml 文件的位置
    * Mapper 接口文件的位置
    
    ```xml
    <?xml version="1.0" encoding="UTF-8" ?>
    <!DOCTYPE generatorConfiguration PUBLIC
            "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
            "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd" >
    <generatorConfiguration>
    
        <!-- 本地数据库驱动程序jar包的全路径  使用时改称自己的本地路径-->
        <classPathEntry
                location="D:\myprogram\apache-maven-3.6.3\repository\mysql\mysql-connector-java\8.0.18\mysql-connector-java-8.0.18.jar"/>
    
    
        <!-- context 是逆向工程的主要配置信息 -->
        <!-- id：起个名字 -->
        <!-- targetRuntime：设置生成的文件适用于那个 mybatis 版本 -->
        <context id="MySQLTables" targetRuntime="MyBatis3">
            <!--定义生成的java类的编码格式-->
            <property name="javaFileEncoding" value="UTF-8"/>
            <!-- 为sql关键字添加分隔符 -->
            <property name="autoDelimitKeywords" value="true"/>
            <property name="beginningDelimiter" value="`"/>
            <property name="endingDelimiter" value="`"/>
    
            <!--是否覆盖xml文件: false 覆盖 true:追加 -->
            <property name="mergeable" value="true"></property>
            <!--分页插件-->
    <!--        <plugin type="org.mybatis.generator.plugins.PaginationPlugin"/>-->
            <!--实现model example 以及内部类序列化插件-->
    <!--        <plugin type="org.mybatis.generator.plugins.SerializablePlugin2"/>-->
            <!--重写equals 、 hashCode-->
            <plugin type="org.mybatis.generator.plugins.EqualsHashCodePlugin"/>
            <!--重写 toString-->
            <plugin type="org.mybatis.generator.plugins.ToStringPlugin"/>
    
    
            <!--optional,指在创建class时，对注释进行控制-->
            <commentGenerator>
                <property name="suppressAllComments" value="true"/><!-- 是否取消注释 -->
                <property name="suppressDate" value="false"/> <!-- 是否生成注释代时间戳 -->
                <property name="swagger" value="false"/><!-- 是否开启swagger注解 -->
            </commentGenerator>
    
            <!-- 数据库的相关配置 -->
            <!--jdbc的数据库连接 mybatis 为数据库名字-->
            <jdbcConnection
                    driverClass="com.mysql.cj.jdbc.Driver"
                    connectionURL="jdbc:mysql://localhost:3306/cloud?useUnicode=true&amp;characterEncoding=UTF-8&amp;autoReconnect=true&amp;autoReconnectForPools=true&amp;serverTimezone=UTC"
                    userId="root"
                    password="root">
                <property name="nullCatalogMeansCurrent" value="true"/>
            </jdbcConnection>
    
            <!--非必须，类型处理器，在数据库类型和java类型之间的转换控制-->
            <javaTypeResolver>
                <!-- 默认情况下数据库中的 decimal，bigInt 在 Java 对应是 sql 下的 BigDecimal 类 -->
                <!-- 不是 double 和 long 类型 -->
                <!-- 使用常用的基本类型代替 sql 包下的引用类型 -->
                <property name="forceBigDecimals" value="false"/>
            </javaTypeResolver>
    
            <!-- 实体类生成的位置 -->
            <!-- targetPackage：生成的实体类所在的包 -->
            <!-- targetProject：生成的实体类所在的硬盘位置 -->
            <javaModelGenerator targetPackage="cn.enn.model" targetProject="src/main/java">
                <!-- 是否允许子包 -->
                <property name="enableSubPackages" value="false"/>
                <!-- 是否对model添加构造函数 -->
                <property name="constructorBased" value="true"/>
                <!-- 是否清理从数据库中查询出的字符串左右两边的空白字符 -->
                <property name="trimStrings" value="true"/>
                <!-- 建立model对象是否不可改变 即生成的modal对象不会有setter方法，只有构造方法 -->
                <property name="immutable" value="false"/>
            </javaModelGenerator>
    
            <!-- Mapper.xml 文件的位置 -->
            <sqlMapGenerator targetPackage="mapper" targetProject="src/main/resources">
                <!-- 针对数据库的一个配置，是否把 schema 作为字包名 -->
                <property name="enableSubPackages" value="false"/>
            </sqlMapGenerator>
    
            <!-- Mapper 接口文件的位置 -->
            <javaClientGenerator targetPackage="cn.enn.mapper" targetProject="src/main/java"
                                 type="XMLMAPPER">
                <property name="enableSubPackages" value="false"/>
            </javaClientGenerator>
    
            <!-- table指定每个生成表的生成策略  表名 和 model实体类名-->
            <!-- tableName是数据库中的表名，domainObjectName是生成的JAVA模型名，
                    后面的参数不用改，要生成更多的表就在下面继续加table标签 -->
            <table tableName="payment" >
            </table>
        </context>
    </generatorConfiguration>
   
    ```
   
 5. 点击 idea 右方,`mybatis-generator` 插件下的 `mybatis-generator:generate`
    * 这里注意，mapper接口和model实体类都可以覆盖，但是对应的 `resources/mapper/*.xml`mapper 映射文件会以追加的形式写入，不会覆盖。
    所以，再进行多次生成的时候，先删除原来的 xml 文件；
    * 复杂查询，建议另写一个mapper2与mapper2.xml, 以防止再次生成时，误删除复杂查询的代码；
    
 6. 对逆向生成的增删改查的使用  
 * 启动时，在SpringBoot 主入口类，加上注解`@MapperScan("cn.enn.mybatisgenerator.mapper")`
 * 具体使用， 参考 [MyBatis的Mapper接口以及Example的实例函数及详解](https://blog.csdn.net/biandous/article/details/65630783)
 
 