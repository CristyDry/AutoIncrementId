# AutoIncrementId
分布式自增ID,唯一ID
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName("127.0.0.1");
        factory.setPort(6379);

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMinIdle(20);
        poolConfig.setMaxIdle(100);
        poolConfig.setMaxTotal(150);
        factory.setPoolConfig(poolConfig);
        factory.afterPropertiesSet();
        final StringRedisTemplate template = new StringRedisTemplate(factory);

        //机器码
        RedisMachineIpFactory machineFactory = new RedisMachineIpFactory();
        machineFactory.setRedis(template);
        machineFactory.init();

        //本地序号
        LocalSerialNumberImpl serialNumber = new LocalSerialNumberImpl();
        serialNumber.setAutoDelete(true);
        serialNumber.setFilePath("E://test");
        //serialNumber.setPattern("yyyyMMdd");//每日重置
        //serialNumber.setPattern("yyyyMMddHH");//每小时重置
        //serialNumber.setPattern("yyyyMMddHHmm");//每分钟重置
        serialNumber.setPattern("yyyyMMddHHmmss");//每秒钟重置

        //Redis
        RedisSerialNumberImpl redisSerialNumber = new RedisSerialNumberImpl(template);
        redisSerialNumber.setPattern("yyyyMMddHHmmss");//每秒钟重置

        //非连续
        WorkerSerialNumberImpl workerSerialNumber = new WorkerSerialNumberImpl(machineFactory.machineId());
        //序号生成
        Unique unique = new Unique(machineFactory,serialNumber,workerSerialNumber);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        while (true){
            //本地自增 编号DNO
            System.out.println(String.format("%s%s%03d%s%05d", "DNO", "00001", unique.machineId(), "20180101", unique.nextSerialId("DNO")));
            //本地自增 编号SNO
            System.out.println(String.format("%s%s%03d%s%05d", "DNO", "00001", unique.machineId(), "20180101", unique.nextSerialId("SNO")));
            //Redis自增 编号SNO
            System.out.println(String.format("%s%s%03d%s%05d", "DNO", "00001", unique.machineId(), "20180101", redisSerialNumber.serialNumber("SNO")));
            //非连续ID
            System.out.println(unique.nextUniqueId(""));
            Thread.sleep(500);
        }