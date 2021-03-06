var CUSTOMER_ACTIONS = (function () {
    function customerActionsFactory(router, customerStore) {
        return {
            showFormForCreateCustomer: function () {
                router.transitionTo("customerNew");
            },

            showFormForEditCustomer: function (customer) {
                switch (customer.type) {
                case "company":
                    router.transitionTo("companyCustomerEdit", {customerId: customer.id});
                    break;
                case "person":
                    router.transitionTo("personCustomerEdit", {customerId: customer.id});
                    break;
                default:
                    throw new Error("Unknown customer type " + customer.type);
                }
            },

            createCompanyCustomer: function (data) {
                customerStore.createCompanyCustomer(data).then(
                    function () {
                        router.transitionTo("customers");
                    },
                    function (err) {
                        alert("An unknown error occurred: " + err);
                    }
                );
            },

            createPersonCustomer: function (data) {
                customerStore.createPersonCustomer(data).then(
                    function () {
                        router.transitionTo("customers");
                    },
                    function (err) {
                        alert("An unknown error occurred: " + err);
                    }
                );
            },

            updateCompanyCustomer: function (customer, data) {
                customerStore.updateCompanyCustomer(customer.id, data).then(
                    function () {
                        router.transitionTo("customers");
                    },
                    function (err) {
                        alert("An unknown error occurred: " + err);
                    }
                );
            },

            updatePersonCustomer: function (customer, data) {
                customerStore.updatePersonCustomer(customer.id, data).then(
                    function () {
                        router.transitionTo("customers");
                    },
                    function (err) {
                        alert("An unknown error occurred: " + err);
                    }
                );
            },

            cancelEditCustomer: function () {
                router.transitionTo("customers");
            }
        }
    }

    return {
        create: customerActionsFactory
    }
}());
