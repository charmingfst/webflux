package com.chm.webflux;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Test {
    public static void main(String[] args) throws Exception{
//        Flux.just("a", "b", "c").map(s -> s.toUpperCase()).subscribe(v-> System.out.println(v));
//        Flux.just("a", "b", "c").flatMap(s -> Mono.just(s.toUpperCase())).subscribe(v-> System.out.println(v));
//        Flux.just(5, 6,7,8,9)
//                .log()
//                .flatMap(e -> {
//                    return Flux.just(e*2).delayElements(Duration.ofSeconds(2));
//                })
//                .subscribe(e -> System.out.println(e)); //订阅者每次向上游request的个数，默认为256
//        TimeUnit.SECONDS.sleep(10);

        // 如果想将每个独立item的处理切换到不同的线程中（上限是线程池的数量），那我们需要将它们打散到不同的publisher中，每个publisher都在一个背景线程中请求结果。
        // 一种方式是使用操作符`flatMap()`，会将所有的items映射进一个Publisher（一般是不同类型的），然后返回新类型的sequence：
        Flux.just("red", "white", "blue", "yellow", "pink")
                .log() //将Flux中的事件被记录到标准输出
                .flatMap(value -> Mono.just(value.toUpperCase()).subscribeOn(Schedulers.newParallel("sub")), 2)
                .publishOn(Schedulers.newParallel("pub"), 2) //从上游Publisher中pre-fetch items，以减少等待时间
                .subscribe(value -> {
                    log.info("Consumed: " + value);
                });
        TimeUnit.SECONDS.sleep(10);
        //第二个参数`concurrency hint`，则确保任何时刻都只有2 items被处理，当然前提是可用。
    }
}
