<!-----------------------------------------------------------------------------
 * Copyright (c) 2021 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
  ----------------------------------------------------------------------------->
<template>
  <span>
    <request-button @on-click="read" title="Composite Read">R</request-button>
    <composite-operation-setting-menu>
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          class="ma-1"
          small
          tile
          min-width="0"
          elevation="0"
          v-bind="attrs"
          v-on="on"
          title="Composite Operation Settings"
        >
          <v-icon small> mdi-tune</v-icon>
        </v-btn>
      </template>
    </composite-operation-setting-menu>
  </span>
</template>
<script>
import RequestButton from "../RequestButton.vue";
import { preference } from "vue-preferences";
import CompositeOperationSettingMenu from "./CompositeOperationSettingMenu.vue";

const timeout = preference("timeout", { defaultValue: 5 });
const compositePathFormat = preference("CompositePathFormat", {
  defaultValue: "SENML_CBOR",
});
const compositeNodeFormat = preference("CompositeNodeFormat", {
  defaultValue: "SENML_CBOR",
});

/**
 * List of Action button to execute operation (Read/Write/Observe ...) on a LWM2M Object Instance.
 */
export default {
  components: { RequestButton, CompositeOperationSettingMenu },
  props: {
    endpoint: String, // endpoint name of the LWM2M client
    compositeObject: Object, // composite object to control
  },
  data() {
    return {
      dialog: false,
    };
  },
  computed: {
    showDialog: {
      get() {
        return this.dialog;
      },
      set(value) {
        this.dialog = value;
        this.$refs.W.resetState();
      },
    },
  },
  methods: {
    requestPath() {
      return `api/clients/${encodeURIComponent(this.endpoint)}/composite`;
    },
    requestOption() {
      return `?timeout=${timeout.get()}&pathformat=${compositePathFormat.get()}&nodeformat=${compositeNodeFormat.get()}`;
    },
    updateState(content, requestButton) {
      let state = !content.valid
        ? "warning"
        : content.success
        ? "success"
        : "error";
      requestButton.changeState(state, content.status);
    },
    read(requestButton) {
      this.axios
        .get(`${this.requestPath()}${this.requestOption()}`, {
          params: { paths: this.compositeObject.paths.join(",") },
        })
        .then((response) => {
          this.updateState(response.data, requestButton);
          if (response.data.success) {
            this.$store.newNodes(this.endpoint, response.data.content);
          }
        })
        .catch(() => {
          requestButton.resetState();
        });
    },
  },
};
</script>
