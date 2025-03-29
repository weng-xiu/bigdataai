package com.bigdataai.datastorage.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * HDFS存储服务接口
 * 负责HDFS文件系统的存储操作
 */
public interface HDFSService {
    
    /**
     * 创建目录
     * @param path 目录路径
     * @return 是否创建成功
     */
    boolean createDirectory(String path);
    
    /**
     * 删除文件或目录
     * @param path 文件或目录路径
     * @param recursive 是否递归删除
     * @return 是否删除成功
     */
    boolean delete(String path, boolean recursive);
    
    /**
     * 检查文件或目录是否存在
     * @param path 文件或目录路径
     * @return 是否存在
     */
    boolean exists(String path);
    
    /**
     * 获取文件或目录信息
     * @param path 文件或目录路径
     * @return 文件或目录信息
     */
    Map<String, Object> getFileInfo(String path);
    
    /**
     * 列出目录内容
     * @param path 目录路径
     * @return 目录内容列表
     */
    List<Map<String, Object>> listDirectory(String path);
    
    /**
     * 读取文件内容
     * @param path 文件路径
     * @return 文件内容的字节数组
     */
    byte[] readFile(String path);
    
    /**
     * 读取文件内容为输入流
     * @param path 文件路径
     * @return 文件内容的输入流
     */
    InputStream readFileAsStream(String path);
    
    /**
     * 写入文件
     * @param path 文件路径
     * @param data 文件内容
     * @param overwrite 是否覆盖已有文件
     * @return 是否写入成功
     */
    boolean writeFile(String path, byte[] data, boolean overwrite);
    
    /**
     * 从输入流写入文件
     * @param path 文件路径
     * @param inputStream 输入流
     * @param overwrite 是否覆盖已有文件
     * @return 是否写入成功
     */
    boolean writeFile(String path, InputStream inputStream, boolean overwrite);
    
    /**
     * 复制文件
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @param overwrite 是否覆盖已有文件
     * @return 是否复制成功
     */
    boolean copyFile(String sourcePath, String targetPath, boolean overwrite);
    
    /**
     * 移动文件
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @param overwrite 是否覆盖已有文件
     * @return 是否移动成功
     */
    boolean moveFile(String sourcePath, String targetPath, boolean overwrite);
    
    /**
     * 获取文件内容摘要
     * @param path 文件路径
     * @return 文件内容摘要（如MD5、SHA-1等）
     */
    String getFileChecksum(String path);
    
    /**
     * 设置文件复制因子
     * @param path 文件路径
     * @param replication 复制因子
     * @return 是否设置成功
     */
    boolean setReplication(String path, short replication);
    
    /**
     * 设置文件权限
     * @param path 文件路径
     * @param permission 权限（如："755"）
     * @return 是否设置成功
     */
    boolean setPermission(String path, String permission);
}