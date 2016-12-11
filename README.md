Protobuf Http Message Converter
===============================

The official spring `ProtobufHttpMessageConverter` uses `JsonFormat`, which is not inline with the Protobuf official document.

This implementation copy the origin `ProtobufHttpMessageConverter` and slightly modified to uses the `JsonFormat` inside the official protobuf project 3.1.0.
