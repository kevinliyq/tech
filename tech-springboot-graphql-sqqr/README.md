Graphql-spqr

* Fork sample from graphql-spqr to support single and batch query
* Support single query
'''graph
query graphqlExample
{
  normal: normalGreeting(person: {
    firstName: "kevin",
    lastName: "li"
  }),
  normal2: normalGreeting(person: {
    firstName: "kevin",
    lastName: "li"
  }),
  
  polite: politeGreeting(customer: {
    firstName: "micheal",
    lastName: "zhang",
    title: MR 
  }),
  
  persons: firstNPersons(count: 5)
  {
    ... person
  }
}

fragment person on Person{
  firstName
    lastName
    socialNetworkAccounts
    {
      networkName
      numberOfConnections
      username
    }
}

* Support batch query, this can't be executed in graphiQL
'''graphql batch
[request1, request2]
'''

* TO DO
  * All DTO is built-in in the project, they to be executed and import by dependencies
  * swagger is good way to reach this goal

