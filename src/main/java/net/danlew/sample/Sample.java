package net.danlew.sample;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

public class Sample {

    public static void main(String[] args) {
        Sources sources = new Sources();

        // "Request" latest data once a second
        Observable.interval(1, TimeUnit.SECONDS)
                .flatMap(__ -> getSources(sources))
                .subscribe(data -> System.out.println("Received: " + data.value));

        // Occasionally clear memory (as if app restarted) so that we must go to disk
        Observable.interval(3, TimeUnit.SECONDS)
                .subscribe(__ -> sources.clearMemory());

        // Java will quit unless we idle
        sleep(15 * 1000);
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    static Observable<Data> getSources(Sources sources) {
        // Create our sequence for querying best available data
        Observable<Data> multiSources = Observable.concat(
                sources.memory(),
                sources.disk(),
                sources.network()
        )
                .filter(data -> data != null && data.isUpToDate())
                .firstElement().toObservable();

        return multiSources;
    }

}
