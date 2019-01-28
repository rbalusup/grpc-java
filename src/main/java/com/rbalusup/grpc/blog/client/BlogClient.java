package com.rbalusup.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    public static void main(String[] args) {
        BlogClient blogClient = new BlogClient();
        blogClient.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        createBlog(channel);

        System.out.println("Shutting Down Channel");
        channel.shutdown();
    }

    private void createBlog(ManagedChannel channel) {
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setAuthorId("Varnitha")
                .setTitle("New blog!")
                .setContent("Hello world this is my first blog!")
                .build();

        CreateBlogResponse createBlogResponse = blogClient.createBlog(CreateBlogRequest.newBuilder()
                .setBlog(blog)
                .build());

        System.out.println("Received create blog response");
        System.out.println(createBlogResponse.toString());

        String blogId = createBlogResponse.getBlog().getId();

        System.out.println("Reading blog....");

        ReadBlogResponse readBlogResponse = blogClient.readBlog(ReadBlogRequest.newBuilder()
                .setBlogId(blogId)
                .build());

        System.out.println(readBlogResponse.toString());

        Blog newBlog = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("Changed Author")
                .setTitle("New blog (updated)!")
                .setContent("Hello world this is my first blog! I've added some more content")
                .build();

        System.out.println("Updating blog...");
        UpdateBlogResponse updateBlogResponse = blogClient.updateBlog(
                UpdateBlogRequest.newBuilder().setBlog(newBlog).build());

        System.out.println("Updated blog");
        System.out.println(updateBlogResponse.toString());

        System.out.println("Deleting blog");
        DeleteBlogResponse deleteBlogResponse = blogClient.deleteBlog(
                DeleteBlogRequest.newBuilder().setBlogId(blogId).build()
        );

        System.out.println("Deleted blog");

        // we list the blogs in our database
        blogClient.listBlog(ListBlogRequest.newBuilder().build()).forEachRemaining(
                listBlogResponse -> System.out.println(listBlogResponse.getBlog().toString())
        );

        // we list the blogs in our database
        blogClient.listBlog(ListBlogRequest.newBuilder().build()).forEachRemaining(
                listBlogResponse -> System.out.println(listBlogResponse.getBlog().toString())
        );
    }
}
