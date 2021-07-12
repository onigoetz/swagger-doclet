package com.tenxerconsulting.swagger.doclet.sample;

import org.eclipse.jetty.servlets.CrossOriginFilter;

import com.google.common.base.Optional;
import com.tenxerconsulting.swagger.doclet.sample.resources.AuthResource;
import com.tenxerconsulting.swagger.doclet.sample.resources.FileResource;
import com.tenxerconsulting.swagger.doclet.sample.resources.GreetingsResource;
import com.tenxerconsulting.swagger.doclet.sample.resources.HttpServletRequestResource;
import com.tenxerconsulting.swagger.doclet.sample.resources.ModelResource;
import com.tenxerconsulting.swagger.doclet.sample.resources.ParentResource;
import com.tenxerconsulting.swagger.doclet.sample.resources.PersonResource;
import com.tenxerconsulting.swagger.doclet.sample.resources.RecursiveResource;
import com.tenxerconsulting.swagger.doclet.sample.resources.RegexPathResource;
import com.tenxerconsulting.swagger.doclet.sample.resources.ResponseResource;
import com.tenxerconsulting.swagger.doclet.sample.resources.SubResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.auth.basic.BasicAuthProvider;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;

public class SampleService extends Service<Configuration> {

	public static void main(String[] args) throws Exception {
		new SampleService().run(args);
	}

	@Override
	public void initialize(Bootstrap<Configuration> bootstrap) {
		bootstrap.addBundle(new AssetsBundle("/apidocs", "/apidocs", "index.html"));
	}

	@Override
	public void run(Configuration configuration, Environment environment) throws Exception {
                environment.addProvider(new BasicAuthProvider<String>(basicCredentials -> Optional.of("USERNAME"), "AuthResource Realm"));

		environment.addFilter(CrossOriginFilter.class, "/*").setInitParam("allowedOrigins", "*")
				.setInitParam("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin")
				.setInitParam("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

		environment.addResource(new AuthResource());
		environment.addResource(new GreetingsResource());
		environment.addResource(new HttpServletRequestResource());
		environment.addResource(new RecursiveResource());
		environment.addResource(new ResponseResource());
		environment.addResource(new ModelResource());
		environment.addResource(new ParentResource());
		environment.addResource(new SubResource());
		environment.addResource(new PersonResource());
		environment.addResource(new FileResource());
		environment.addResource(new RegexPathResource());
	}
}
