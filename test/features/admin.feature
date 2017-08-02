Feature: Handle login and logout of admin users
  Scenario: Login
    Given the service is running
    When I retrieve the user 'admin@email.com' with the password 'admin123'
    Then I should get a '200' response
    And the response must contain 'jwt'

  Scenario: Login with wrong password
    Given the service is running
    When I retrieve the user 'admin@email.com' with the password 'abc123'
    Then I should get a '402' response

  Scenario: Login non existing user
    Given the service is running
    When I retrieve the user 'me@email.com' with the password 'me123'
    Then I should get a '404' response

  Scenario: Logout
    Given the user 'admin@email.com' is logged in with password 'admin123'
    When I try to log out
    Then I should get a '200' response

  Scenario: List products
    Given the user 'admin@email.com' is logged in with password 'admin123'
    When I try to retrieve all products
    Then I should get a '200' response
    And the response must contain 'products'

  Scenario: List a specific product
    Given the user 'admin@email.com' is logged in with password 'admin123'
    When I try to retrieve a product with id '1'
    Then I should get a '200' response
    And the response must contain 'product'

  Scenario: List a non existent product
    Given the user 'admin@email.com' is logged in with password 'admin123'
    When I try to retrieve a product with id '99'
    Then I should get a '404' response

  Scenario: Update a product
    Given the user 'admin@email.com' is logged in with password 'admin123'
    When I try to change the 'name' to 'NEW NAME' of a product with id '1'
    Then I should get a '200' response
    And the 'name' should be 'NEW NAME' for the product with id '1'
