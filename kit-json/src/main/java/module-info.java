/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * JSON conversion and structural comparison backed by Jackson and Gson.
 * Backend classes and Kit type tokens are transitively readable because they appear in the public API.
 * Consumer model packages may need to be opened to the backend that accesses them reflectively.
 *
 * @author allurx
 */
module io.allurx.kit.json {
    exports io.allurx.kit.json;
    requires transitive io.allurx.kit.base;
    requires transitive com.google.gson;
    requires transitive tools.jackson.databind;
}
