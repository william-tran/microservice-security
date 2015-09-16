package microsec.freddysbbq.admin_app;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import microsec.freddysbbq.menu.model.v1.MenuItem;

@SpringBootApplication
@Controller
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void halConfig() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new Jackson2HalModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(objectMapper);
        restTemplate.setMessageConverters(Arrays.asList(converter));
    }

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("username", "stub");
        return "index";
    }

    @RequestMapping("/menuItems")
    public String menu(Model model) throws Exception {
        PagedResources<MenuItem> menu = restTemplate
                .exchange(
                        "http://localhost:8083/menuItems",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<PagedResources<MenuItem>>() {
                        })
                .getBody();
        model.addAttribute("menu", menu.getContent());
        return "menu";
    }

    @RequestMapping("/menuItems/new")
    public String newMenuItem(Model model) throws Exception {
        model.addAttribute(new MenuItem());
        return "menuItem";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/menuItems/new/")
    public String saveNewMenuItem(@ModelAttribute MenuItem menuItem) throws Exception {
        restTemplate
                .postForEntity("http://localhost:8083/menuItems/", menuItem, Void.class);
        return "redirect:..";
    }

    @RequestMapping("/menuItems/{id}")
    public String viewMenuItem(Model model, @PathVariable String id) throws Exception {
        Resource<MenuItem> item = restTemplate
                .exchange(
                        "http://localhost:8083/menuItems/{id}",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<Resource<MenuItem>>() {
                        }, id)
                .getBody();
        model.addAttribute("menuItem", item.getContent());
        return "menuItem";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/menuItems/{id}")
    public String saveMenuItem(@PathVariable String id, @ModelAttribute MenuItem menuItem) throws Exception {
        restTemplate.put("http://localhost:8083/menuItems/{id}", menuItem, id);
        return "redirect:..";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/menuItems/{id}/delete")
    public String deleteMenuItem(@PathVariable String id, @ModelAttribute MenuItem menuItem) throws Exception {
        restTemplate.delete("http://localhost:8083/menuItems/{id}", id);
        return "redirect:..";
    }

}