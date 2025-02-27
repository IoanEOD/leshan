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
  <v-data-table
    :headers="headers"
    :items="securityInfos"
    item-key="endpoint"
    sort-by="endpoint"
    class="elevation-0"
    dense
    :search="search"
  >
    <template v-slot:top>
      <v-toolbar flat>
        <v-toolbar-title v-if="$vuetify.breakpoint.smAndUp"
          >Security Information</v-toolbar-title
        >
        <v-divider
          class="mx-4"
          inset
          vertical
          v-if="$vuetify.breakpoint.smAndUp"
        ></v-divider>
        <v-text-field
          v-model="search"
          append-icon="mdi-magnify"
          label="Search"
          single-line
          hide-details
          class="pa-2"
          clearable
        ></v-text-field>
        <!-- add security info button-->
        <v-btn dark class="mb-2" @click.stop="openNewSec()">
          {{ $vuetify.breakpoint.smAndDown ? "" : "Add Security Information" }}
          <v-icon :right="!$vuetify.breakpoint.smAndDown" dark>
            mdi-key-plus
          </v-icon>
        </v-btn>

        <!-- add/edit security info dialog -->
        <security-info-dialog
          v-model="dialogOpened"
          @new="newSec($event)"
          @edit="editSec($event)"
          :initialValue="editedSecurityInfo"
        />
      </v-toolbar>
    </template>
    <!--custom display for "mode" column-->
    <template v-slot:item.mode="{ item }">
      <security-info-chip :securityInfo="item" />
    </template>
    <!--custom display for "details" column-->
    <template v-slot:item.details="{ item }">
      <div v-if="item.mode == 'psk'" style="word-break:break-all;" class="pa-1">
        <strong>Identity:</strong> <code>{{ item.details.identity }}</code>
        <br />
        <strong>Key:</strong
        ><code class="text-uppercase">{{ item.details.key }}</code>
      </div>
      <div v-if="item.mode == 'rpk'" style="word-break:break-all;" class="pa-1">
        <strong>Public Key:</strong>
        <code class="text-uppercase">{{ item.details.key }}</code>
      </div>
      <div
        v-if="item.mode == 'x509'"
        style="word-break:break-all;"
        class="pa-1"
      >
        <strong>X509 certificate with CN equals :</strong>
        <code>{{ item.endpoint }}</code>
      </div>
    </template>
    <!--custom display for "actions" column-->
    <template v-slot:item.actions="{ item }">
      <v-icon
        small
        class="mr-2"
        @click.stop="openEditSec(item)"
        :disabled="item.mode == 'unsupported'"
      >
        mdi-pencil
      </v-icon>
      <v-icon small @click="deleteSec(item)">
        mdi-delete
      </v-icon>
    </template>
  </v-data-table>
</template>
<script>
import SecurityInfoDialog from "@leshan-server-core-demo/components/security/SecurityInfoDialog.vue";
import SecurityInfoChip from "@leshan-server-core-demo/components/security/SecurityInfoChip.vue";
import {
  adaptToUI,
  adaptToAPI,
} from "@leshan-server-core-demo/js/securityutils.js";

export default {
  components: { SecurityInfoDialog, SecurityInfoChip },
  data: () => ({
    dialogOpened: false,
    headers: [
      { text: "Endpoint", value: "endpoint" },
      { text: "Security mode", value: "mode" },
      { text: "Details", value: "details", sortable: false, width: "60%" },
      { text: "Actions", value: "actions", sortable: false },
    ],
    search: "",
    securityInfos: [],
    editedSecurityInfo: {}, // initial value for Security Information dialog
  }),

  beforeMount() {
    this.axios.get("api/security/clients").then(
      (response) =>
        (this.securityInfos = response.data.map((c) => {
          return adaptToUI(c);
        }))
    );
  },

  methods: {
    openNewSec() {
      this.editedSecurityInfo = null;
      this.dialogOpened = true;
    },

    newSec(cred) {
      this.axios
        .put("api/security/clients/", adaptToAPI(cred, cred.endpoint))
        .then(() => {
          this.securityInfos.push(cred);
          this.dialogOpened = false;
        });
    },

    openEditSec(sec) {
      this.editedSecurityInfo = sec;
      this.dialogOpened = true;
    },

    editSec(sec) {
      this.axios
        .put("api/security/clients/", adaptToAPI(sec, sec.endpoint))
        .then(() => {
          this.securityInfos = this.securityInfos.map((s) =>
            s.endpoint == sec.endpoint ? sec : s
          );
          this.dialogOpened = false;
        });
    },

    deleteSec(sec) {
      this.indexToRemove = this.securityInfos.indexOf(sec);
      this.axios
        .delete("api/security/clients/" + encodeURIComponent(sec.endpoint))
        .then(() => {
          this.securityInfos.splice(this.indexToRemove, 1);
        });
    },
  },
};
</script>
