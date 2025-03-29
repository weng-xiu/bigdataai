package com.bigdataai.datastorage.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HDFS存储服务实现类
 */
@Service
public class HDFSServiceImpl implements HDFSService {

    @Value("${hadoop.fs.defaultFS}")
    private String defaultFS;

    /**
     * 获取HDFS配置
     */
    private Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", defaultFS);
        return configuration;
    }

    /**
     * 获取HDFS文件系统
     */
    private FileSystem getFileSystem() throws IOException {
        return FileSystem.get(getConfiguration());
    }

    @Override
    public boolean createDirectory(String path) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path hdfsPath = new Path(path);
            return fileSystem.mkdirs(hdfsPath);
        } catch (IOException e) {
            throw new RuntimeException("创建HDFS目录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String path, boolean recursive) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path hdfsPath = new Path(path);
            return fileSystem.delete(hdfsPath, recursive);
        } catch (IOException e) {
            throw new RuntimeException("删除HDFS文件或目录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String path) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path hdfsPath = new Path(path);
            return fileSystem.exists(hdfsPath);
        } catch (IOException e) {
            throw new RuntimeException("检查HDFS文件或目录是否存在失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getFileInfo(String path) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path hdfsPath = new Path(path);
            
            if (!fileSystem.exists(hdfsPath)) {
                return null;
            }
            
            FileStatus status = fileSystem.getFileStatus(hdfsPath);
            Map<String, Object> fileInfo = new HashMap<>();
            
            fileInfo.put("path", status.getPath().toString());
            fileInfo.put("isDirectory", status.isDirectory());
            fileInfo.put("length", status.getLen());
            fileInfo.put("modificationTime", status.getModificationTime());
            fileInfo.put("accessTime", status.getAccessTime());
            fileInfo.put("owner", status.getOwner());
            fileInfo.put("group", status.getGroup());
            fileInfo.put("permission", status.getPermission().toString());
            fileInfo.put("replication", status.getReplication());
            fileInfo.put("blockSize", status.getBlockSize());
            
            return fileInfo;
        } catch (IOException e) {
            throw new RuntimeException("获取HDFS文件信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> listDirectory(String path) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path hdfsPath = new Path(path);
            
            if (!fileSystem.exists(hdfsPath)) {
                return null;
            }
            
            if (!fileSystem.isDirectory(hdfsPath)) {
                throw new IllegalArgumentException("路径不是目录: " + path);
            }
            
            FileStatus[] statuses = fileSystem.listStatus(hdfsPath);
            List<Map<String, Object>> fileList = new ArrayList<>();
            
            for (FileStatus status : statuses) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("path", status.getPath().toString());
                fileInfo.put("name", status.getPath().getName());
                fileInfo.put("isDirectory", status.isDirectory());
                fileInfo.put("length", status.getLen());
                fileInfo.put("modificationTime", status.getModificationTime());
                fileInfo.put("owner", status.getOwner());
                fileInfo.put("group", status.getGroup());
                fileInfo.put("permission", status.getPermission().toString());
                fileList.add(fileInfo);
            }
            
            return fileList;
        } catch (IOException e) {
            throw new RuntimeException("列出HDFS目录内容失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] readFile(String path) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path hdfsPath = new Path(path);
            
            if (!fileSystem.exists(hdfsPath)) {
                return null;
            }
            
            if (fileSystem.isDirectory(hdfsPath)) {
                throw new IllegalArgumentException("路径是目录，不是文件: " + path);
            }
            
            try (FSDataInputStream inputStream = fileSystem.open(hdfsPath);
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException("读取HDFS文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream readFileAsStream(String path) {
        try {
            FileSystem fileSystem = getFileSystem();
            Path hdfsPath = new Path(path);
            
            if (!fileSystem.exists(hdfsPath)) {
                return null;
            }
            
            if (fileSystem.isDirectory(hdfsPath)) {
                throw new IllegalArgumentException("路径是目录，不是文件: " + path);
            }
            
            return fileSystem.open(hdfsPath);
        } catch (IOException e) {
            throw new RuntimeException("读取HDFS文件为流失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean writeFile(String path, byte[] data, boolean overwrite) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path hdfsPath = new Path(path);
            
            // 检查文件是否存在且不允许覆盖
            if (fileSystem.exists(hdfsPath) && !overwrite) {
                return false;
            }
            
            try (FSDataOutputStream outputStream = fileSystem.create(hdfsPath, overwrite)) {
                outputStream.write(data);
                outputStream.flush();
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException("写入HDFS文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean writeFile(String path, InputStream inputStream, boolean overwrite) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path hdfsPath = new Path(path);
            
            // 检查文件是否存在且不允许覆盖
            if (fileSystem.exists(hdfsPath) && !overwrite) {
                return false;
            }
            
            try (FSDataOutputStream outputStream = fileSystem.create(
                    hdfsPath,
                    overwrite,
                    4096,
                    fileSystem.getDefaultReplication(hdfsPath),
                    fileSystem.getDefaultBlockSize(hdfsPath),
                    new Progressable() {
                        @Override
                        public void progress() {
                            // 进度回调，可以用于显示进度
                        }
                    }
            )) {
                IOUtils.copyBytes(inputStream, outputStream, 4096);
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException("从流写入HDFS文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean copyFile(String sourcePath, String targetPath, boolean overwrite) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path sourceHdfsPath = new Path(sourcePath);
            Path targetHdfsPath = new Path(targetPath);
            
            // 检查源文件是否存在
            if (!fileSystem.exists(sourceHdfsPath)) {
                return false;
            }
            
            // 检查目标文件是否存在且不允许覆盖
            if (fileSystem.exists(targetHdfsPath) && !overwrite) {
                return false;
            }
            
            // 使用Hadoop的文件复制功能
            return fileSystem.rename(sourceHdfsPath, targetHdfsPath);
        } catch (IOException e) {
            throw new RuntimeException("复制HDFS文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean moveFile(String sourcePath, String targetPath, boolean overwrite) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path sourceHdfsPath = new Path(sourcePath);
            Path targetHdfsPath = new Path(targetPath);
            
            // 检查源文件是否存在
            if (!fileSystem.exists(sourceHdfsPath)) {
                return false;
            }
            
            // 检查目标文件是否存在且不允许覆盖
            if (fileSystem.exists(targetHdfsPath) && !overwrite) {
                return false;
            }
            
            // 使用Hadoop的文件移动功能
            return fileSystem.rename(sourceHdfsPath, targetHdfsPath);
        } catch (IOException e) {
            throw new RuntimeException("移动HDFS文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileChecksum(String path) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path hdfsPath = new Path(path);
            
            if (!fileSystem.exists(hdfsPath)) {
                return null;
            }
            
            if (fileSystem.isDirectory(hdfsPath)) {
                throw new IllegalArgumentException("路径是目录，不是文件: " + path);
            }
            
            return fileSystem.getFileChecksum(hdfsPath).toString();
        } catch (IOException e) {
            throw new RuntimeException("获取HDFS文件校验和失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean setReplication(String path, short replication) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path hdfsPath = new Path(path);
            
            if (!fileSystem.exists(hdfsPath)) {
                return false;
            }
            
            if (fileSystem.isDirectory(hdfsPath)) {
                throw new IllegalArgumentException("路径是目录，不是文件: " + path);
            }
            
            return fileSystem.setReplication(hdfsPath, replication);
        } catch (IOException e) {
            throw new RuntimeException("设置HDFS文件复制因子失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean setPermission(String path, String permission) {
        try (FileSystem fileSystem = getFileSystem()) {
            Path hdfsPath = new Path(path);
            
            if (!fileSystem.exists(hdfsPath)) {
                return false;
            }
            
            FsPermission fsPermission = new FsPermission(permission);
            fileSystem.setPermission(hdfsPath, fsPermission);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("设置HDFS文件权限失败: " + e.getMessage(), e);
        }
    }