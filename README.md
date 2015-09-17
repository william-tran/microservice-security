# Freddy's BBQ Joint

![diagram](docs/diagram.gif "Diagram")

This demo is an example of using Pivotal SSO and Spring Cloud Services in a microservice architecture.

Here are the actors:
- Freddy, owner of Freddy’s BBQ Joint, the best ribs in DC
- Frank, Freddy’s most important customer (and the most powerful man in the world)
- The Developer, works for Frank and wants to impress him with a side project (this app)

This is the use case:
- Give Frank the ability to see the menu online and place orders
- Give Freddy the ability to manage the menu and close orders

The commits in this repo outline how to create, secure, and test the security of microservices, and add support for Pivotal SSO and Spring Cloud Services, one step at a time. All the development and testing can run locally, and about 2/3rds the way through we start pushing to CF. We start with a single application and evolve the architecture to 2 UI applicaitons and 2 API applications.

