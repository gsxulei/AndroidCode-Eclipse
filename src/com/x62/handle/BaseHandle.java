package com.x62.handle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Message;

/**
 * 基本数据处理封装
 * 
 * @author GSXL
 *
 * @param <P>
 *            参数
 * @param <S>
 *            处理成功回调参数
 * @param <F>
 *            处理失败回调参数
 */
public abstract class BaseHandle<P,S,F>
{
	private final int SUCCESS=0;
	private final int FAIL=1;

	private Map<String,CallBack<S,F>> cbs=new HashMap<String,CallBack<S,F>>();
	protected ExecutorService es=Executors.newCachedThreadPool();
	private Map<String,S> ss=new HashMap<String,S>();
	private Map<String,F> fs=new HashMap<String,F>();

	private Handler handler=new Handler()
	{
		public void handleMessage(Message msg)
		{
			String key=""+msg.arg1;
			CallBack<S,F> cb=cbs.get(key);
			if(cb==null)
			{
				return;
			}
			removeCallBack(cb);
			switch(msg.what)
			{
				case SUCCESS:
				{
					S s=ss.get(key);
					cb.onSuccess(s);
				}
				break;
				case FAIL:
				{
					F f=fs.get(key);
					cb.onFail(f);
				}
				break;
			}
		}
	};

	protected void addCallBack(CallBack<S,F> cb)
	{
		if(cb==null)
		{
			return;
		}
		cbs.put(""+cb.hashCode(),cb);
	}

	protected void removeCallBack(CallBack<S,F> cb)
	{
		if(cb==null)
		{
			return;
		}
		cbs.remove(""+cb.hashCode());
	}

	public void exec(final P p,final CallBack<S,F> cb)
	{
		addCallBack(cb);
		es.execute(new Runnable()
		{
			@Override
			public void run()
			{
				exec(p,cb.hashCode());
			}
		});
	}

	protected abstract void exec(P p,int key);

	protected void postSuccess(int key,S s)
	{
		Message msg=Message.obtain();
		msg.what=SUCCESS;
		msg.arg1=key;
		ss.put(""+key,s);
		handler.sendMessage(msg);
	}

	protected void postFail(int key,F f)
	{
		Message msg=Message.obtain();
		msg.what=FAIL;
		msg.arg1=key;
		fs.put(key+"",f);
		handler.sendMessage(msg);
	}
}