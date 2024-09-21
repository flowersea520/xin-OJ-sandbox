package org.example.judgehubcodesandbox.docker;

import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PullResponseItem;

public class MyPullImageCallback extends PullImageResultCallback {
    /**
     * onNext 方法在镜像拉取过程中被调用，每次从 Docker 拉取操作中获得一个新的响应项时，都会触发这个方法。
     * @param item
     */
    @Override
    public void onNext(PullResponseItem item) {
        // 打印镜像拉取的日志信息
        System.out.println("下载镜像：" + item.getStatus());
        // 最终还是执行父类的 逻辑
        super.onNext(item);
    }

    @Override
    public void onComplete() {
        // 镜像拉取完成后执行
        System.out.println("镜像下载完成");
    }
}
