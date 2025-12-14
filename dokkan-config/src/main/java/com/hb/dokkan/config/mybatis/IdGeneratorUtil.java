package com.hb.dokkan.config.mybatis;

import java.util.UUID;

/**
 * 16位短UUID生成器工具类。
 * <p>
 * 通过截取标准UUID的一部分来生成一个定长的、不易重复的ID。
 * 这种方法生成的ID是基于一个64位的长整型，碰撞概率远低于普通随机数。
 */
public final class IdGeneratorUtil {

    /**
     * 私有构造函数，防止该工具类被实例化。
     */
    private IdGeneratorUtil() {
        // 防止通过反射实例化
        throw new IllegalStateException("Utility class");
    }

    /**
     * 生成一个16位的随机ID。
     *
     * @return 16位的十六进制字符串ID
     */
    public static String generate16CharUuid() {
        // 1. 调用Java自带的UUID生成器，生成一个128位的UUID
        UUID uuid = UUID.randomUUID();

        // 2. 获取UUID的最高64位，这是一个long类型的数值
        //    这64位包含了足够高的随机性
        long mostSigBits = uuid.getMostSignificantBits();

        // 3. 将64位的long值转换为16位的十六进制字符串
        //    - 使用Long.toHexString()可以直接转换
        //    - 但是为了保证输出总是16位（例如，当高位为0时），需要手动处理一下格式
        //    - 下面的方法可以优雅地保证输出为16位，不足的前面补0
        char[] hexChars = new char[16];
        for (int i = 0; i < 16; i++) {
            // 从最高位开始，每次取4个bit转换为一个十六进制字符
            int B_BIT_OFFSET = (15 - i) * 4;
            int hexValue = (int) ((mostSigBits >>> B_BIT_OFFSET) & 0x0f);
            hexChars[i] = Character.forDigit(hexValue, 16);
        }
        return new String(hexChars);
    }

    /**
     * 【备选方案】一个更简洁的实现方式，但可读性稍差。
     * 它利用了String.format的格式化功能来补全前导零。
     *
     * @return 16位的十六进制字符串ID
     */
    public static String generate16CharUuidSimple() {
        long mostSigBits = UUID.randomUUID().getMostSignificantBits();
        // %016x 表示输出一个16位的十六进制数，不足16位的前面补0
        return String.format("%016x", mostSigBits);
    }

}