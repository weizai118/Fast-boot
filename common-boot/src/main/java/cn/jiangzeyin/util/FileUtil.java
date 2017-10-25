package cn.jiangzeyin.util;

import org.springframework.util.Assert;

import java.io.*;

/**
 * 文件操作工具类
 *
 * @author jiangzeyin
 */
public final class FileUtil {

    /**
     * @param inputStream inp
     * @param file        file
     * @return boolean
     * @throws IOException io
     */
    public static boolean writeInputStream(InputStream inputStream, File file) throws IOException {
        Assert.notNull(inputStream);
        Assert.notNull(file);
        File parent = file.getParentFile();
        if (!parent.exists())
            if (!parent.mkdirs())
                throw new IllegalArgumentException(file.getPath() + " create fail");
        DataOutputStream outputStream = null;
        try {
            outputStream = new DataOutputStream(new FileOutputStream(file));
            int len = inputStream.available();
            //判断长度是否大于1M
            if (len <= 1024 * 1024) {
                byte[] bytes = new byte[len];
                int rLen = inputStream.read(bytes);
                if (rLen > 0)
                    outputStream.write(bytes);
            } else {
                int byteCount;
                //1M逐个读取
                byte[] bytes = new byte[1024 * 1024];
                while ((byteCount = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, byteCount);
                }
            }
        } finally {
            inputStream.close();
            if (outputStream != null)
                outputStream.close();
        }
        return true;
    }

    public static String clearPath(String input) {
        input = input.replace('\\', '/');
        return clearPath_(input);
    }


    private static String clearPath_(String input) {
        int from = 0;
        int j = input.indexOf("://");
        if (j != -1) {
            from = j + 3;
        }
        int i = input.indexOf("//", from);
        if (i == -1) {
            return input;
        }

        String input_ = input.substring(0, i) + "/" + input.substring(i + 2);
        return clearPath_(input_);
    }
}

