package com.unique.id.util;

//序号生成器
public class IdWorker {

    private static final long DEFAULT_WORKER_BITS=10L;
    private static final long DEFAULT_SEQUENCE_BITS=12L;
    //机器码
    private final long workerId;
    //机器码位数
    private long workerIdBits;
    //序号位数
    private long sequenceBits;
    //开始时间戳
    private final static long twepoch = 1361753741828L;
    //序号
    private long sequence = 0L;
    //最大机器码
    private final long maxWorkerId;
    //机器码位移数
    private final long workerIdShift;
    //时间戳位移数
    private final long timestampLeftShift;
    //最大序号
    private final long sequenceMask;

    private long lastTimestamp = -1L;

    public IdWorker(final long workerId) {
        super();
        workerIdBits = DEFAULT_WORKER_BITS;
        sequenceBits = DEFAULT_SEQUENCE_BITS;
        //最大机器码
        maxWorkerId = -1L ^ -1L << workerIdBits;
        //时间戳位移数
        timestampLeftShift = sequenceBits + workerIdBits;
        //最大序号
        sequenceMask = -1L ^ -1L << sequenceBits;
        //机器码位移数
        workerIdShift = sequenceBits;
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format(
                    "worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        this.workerId = workerId;
    }

    public IdWorker(final long workerId, long workerIdBits, long sequenceBits) {
        super();
        //最大机器码
        maxWorkerId = -1L ^ -1L << workerIdBits;
        //时间戳位移数
        timestampLeftShift = sequenceBits + workerIdBits;
        //最大序号
        sequenceMask = -1L ^ -1L << sequenceBits;
        //机器码位移数
        workerIdShift = sequenceBits;
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format(
                    "worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        long timestamp = this.timeGen();
        if (this.lastTimestamp == timestamp) {
            this.sequence = (this.sequence + 1) & sequenceMask;
            if (this.sequence == 0) {
                timestamp = this.tilNextMillis(this.lastTimestamp);
            }
        } else {
            this.sequence = 0;
        }
        if (timestamp < this.lastTimestamp) {
            return 0;
        }

        this.lastTimestamp = timestamp;
        long nextId = ((timestamp - twepoch << timestampLeftShift))
                | (this.workerId << workerIdShift) | (this.sequence);
        return nextId;
    }

    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }


}