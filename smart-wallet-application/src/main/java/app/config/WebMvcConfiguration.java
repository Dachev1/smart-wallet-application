package app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import app.security.SessionCheckInterceptor;


@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private SessionCheckInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                //everything after: "/**"
                .addPathPatterns("/**")
                .addPathPatterns("/css/**", "/images/**");
    }

}
