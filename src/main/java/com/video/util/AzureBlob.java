package com.video.util;


import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.MessageFormat;


/**
 * @author
 * @version 1.0
 * @date 2020-07-14 16:16
 */
public class AzureBlob {
    private static String ACCOUNT_NAME = "faker";
    private static String ACCOUNT_KEY = "";
    private static String END_POINT = "core.windows.net";
    private static String PROTOCOL = "https";
    private static String format = "DefaultEndpointsProtocol={0};AccountName={1};AccountKey={2};EndpointSuffix={3}";

    private static String serverUrl = "https://faker.blob.core.windows.net/video";
    private static CloudStorageAccount storageAccount = null;
    private static CloudBlobClient blobClient = null;
    private static CloudBlobContainer container = null;

    public static void main(String[] args) {
        // initAzure("video");
        // File file = new File("C:\\Users\\CQG\\Desktop\\upload\\20231118\\字段-数据类型.txt");
        // uploadFile(file);
        listBlobs(null);

    }

    static {
        initAzure("video");
    }

    public static void listBlobs(String perfix) {
        /**
         * 第一个参数, container中blob的前缀, 可以是文件夹的前缀, 也可以是blob的前缀
         * 第二个参数, 是否展开文件夹中的文件, 如container中无文件夹, 则会列出所有blob
         */
        Iterable<ListBlobItem> blobItems = container.listBlobs(null, true);
        for (ListBlobItem blobItem : blobItems) {
            String uri = blobItem.getUri().toString();
            System.out.println(uri);
        }
    }

    public static String uploadFile(File file,String visitUrl) {
        try {
            // 构建目标BlockBlob对象
            CloudBlockBlob blob = container.getBlockBlobReference(visitUrl.substring(1,visitUrl.length()));
            // 将本地文件上传到Azure Container
            blob.uploadFromFile(file.getPath());
            // 获得获得属性
            blob.downloadAttributes();
            // 获得上传后的文件大小
            long blobSize = blob.getProperties().getLength();
            // 获得本地文件大小
            long localSize = file.length();
            // 校验
            if (blobSize != localSize) {
                System.out.println("校验失败...上传失败");
                // 删除blob
                blob.deleteIfExists();
            } else {
                System.out.println("上传成功");
            }
        } catch (URISyntaxException | StorageException | IOException e) {
            e.printStackTrace();
        }
        return serverUrl + visitUrl;
    }

    public static void downloadFile(String blobPath, String targetPath) {
        String finalPath = targetPath.concat(blobPath);
        try {
            // 传入要blob的path
            CloudBlockBlob blob = container.getBlockBlobReference(blobPath);
            // 传入目标path
            blob.downloadToFile(finalPath);
        } catch (URISyntaxException | StorageException | IOException e) {
            e.printStackTrace();
        }
    }


    public static void initAzure(String containerName) {
        try {
            // 获得StorageAccount对象
            storageAccount = CloudStorageAccount.parse(MessageFormat.format(format, PROTOCOL, ACCOUNT_NAME, ACCOUNT_KEY, END_POINT));
            // 由StorageAccount对象创建BlobClient
            blobClient = storageAccount.createCloudBlobClient();
            // 根据传入的containerName, 获得container实例
            container = blobClient.getContainerReference(containerName);
        } catch (URISyntaxException | InvalidKeyException | StorageException e) {
            e.printStackTrace();
        }
    }
}


