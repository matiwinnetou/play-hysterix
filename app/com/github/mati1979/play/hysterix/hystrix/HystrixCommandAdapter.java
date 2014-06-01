package com.github.mati1979.play.hysterix.hystrix;

import com.github.mati1979.play.hysterix.HysterixContext;
import com.github.mati1979.play.hysterix.HysterixResponse;
import com.github.mati1979.play.hysterix.HysterixResponseMetadata;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import play.libs.F;
import rx.Observable;
import rx.Subscriber;

import java.util.Optional;

/**
 * Created by mati on 01/06/2014.
 */
public abstract class HystrixCommandAdapter<T> {

    protected abstract F.Promise<T> run();

    private Proxy proxy;

    protected HystrixCommandAdapter(final HysterixContext hysterixContext) {
        this.proxy = new Proxy();
    }

    protected HystrixCommandAdapter() {
        this.proxy = new Proxy();
    }

    public Proxy getProxy() {
        return proxy;
    }

    public Optional<String> getCommandGroupKey() {
        return Optional.empty();
    }

    public Optional<String> getCacheKey() {
        return Optional.empty();
    }

    public Optional<String> getRemoteUrl() {
        return Optional.empty();
    }

    public F.Promise<HysterixResponse<T>> execute() {
        return toPromise(proxy.observe())
                .map(resp -> HysterixResponse.create(resp, new HysterixResponseMetadata()));
    }

    public abstract String getCommandKey();

    public Optional<F.Promise<T>> getFallbackTo() {
        final Optional<T> fallback = getFallback();

        return fallback.map(value -> F.Promise.pure(value));
    }

    public Optional<T> getFallback() {
        return Optional.empty();
    }

    public static <T> F.Promise<T> toPromise(final Observable<T> os) {
        final scala.concurrent.Promise<T> scalaPromise = scala.concurrent.Promise$.MODULE$.<T>apply();
        os.subscribe(obj -> scalaPromise.success(obj), t -> scalaPromise.failure(t));

        return F.Promise.<T>wrap(scalaPromise.future());
    }

    public static <T> Observable<T> toObs(final F.Promise<T> promise) {
        return Observable.create((Subscriber<? super T> subscriber) -> {
            promise.onRedeem(data -> {
                subscriber.onNext(data);
                subscriber.onCompleted();
            });
            promise.onFailure(t -> subscriber.onError(t));
        });
    }

    private class Proxy extends HystrixObservableCommand<T> {

        public Proxy() {
            super(Setter
                    .withGroupKey(HystrixCommandGroupKey.Factory.asKey(HystrixCommandAdapter.this.getCommandGroupKey().orElse("")))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(HystrixCommandAdapter.this.getCommandKey())));
        }

        @Override
        protected Observable<T> run() {
            return toObs(HystrixCommandAdapter.this.run());
        }

        @Override
        protected String getCacheKey() {
            return HystrixCommandAdapter.this.getCacheKey().orElse(null);
        }

        @Override
        protected Observable<T> getFallback() {
            return HystrixCommandAdapter.this.getFallbackTo()
                    .map(promise -> toObs(promise))
                    .orElseThrow(() -> new UnsupportedOperationException("no fallback"));
        }

    }

}
