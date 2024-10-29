package jbin.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jbin.util.DI;

@WebListener
public class CacheWebListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.setErr(System.out);
		ServletContextListener.super.contextInitialized(sce);
		DI.current().loadAll();
	}
}
