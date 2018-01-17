import com.unique.id.Unique;
import com.unique.id.service.impl.serial.LocalSerialNumberImpl;
import com.unique.id.service.impl.machine.RedisMachineIpFactory;
import com.unique.id.service.impl.serial.RedisSerialNumberImpl;
import com.unique.id.service.impl.unique.WorkerSerialNumberImpl;
import org.junit.Test;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StopWatch;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by LeWis on 2018/1/17.
 */
public class TestUnique {

    //本地序号
    @Test
    public void testLocalAutoIncrease() {
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
        serialNumber.setFilePath("E://");
        //serialNumber.setPattern("yyyyMMdd");//每日重置
        serialNumber.setPattern("yyyyMMddHH");//每小时重置
        //serialNumber.setPattern("yyyyMMddHHmm");//每分钟重置
        //非连续
        WorkerSerialNumberImpl workerSerialNumber = new WorkerSerialNumberImpl(machineFactory.machineId());
        //序号生成
        Unique unique = new Unique(machineFactory,serialNumber,workerSerialNumber);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < 10; i++) {
            //有序
            System.out.println(String.format("%s%s%03d%s%05d", "DNO", "00001", unique.machineId(), "20180101", unique.nextSerialId("DNO")));
            //非连续
            System.out.println(unique.nextUniqueId(""));
        }
        stopWatch.stop();
        System.out.println(stopWatch.toString());
    }

    //redis序号
    @Test
    public void testRedisAutoIncrease() {
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
        RedisSerialNumberImpl serialNumber = new RedisSerialNumberImpl(template);
        //serialNumber.setPattern("yyyyMMdd");//每日重置
        serialNumber.setPattern("yyyyMMddHH");//每小时重置
        //serialNumber.setPattern("yyyyMMddHHmm");//每分钟重置

        //非连续
        WorkerSerialNumberImpl workerSerialNumber = new WorkerSerialNumberImpl(machineFactory.machineId());

        //序号生成
        Unique unique = new Unique(machineFactory, serialNumber, workerSerialNumber);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < 10; i++) {
            System.out.println(String.format("%s%s%03d%s%05d", "DNO", "00001", unique.machineId(), "20180101", unique.nextSerialId("DAP")));
            //非连续
            System.out.println(unique.nextUniqueId(""));
        }
        stopWatch.stop();
        System.out.println(stopWatch.toString());
    }
}
