/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.plugin.parser.ingestion.serializer;

import com.stratio.decision.commons.avro.Action;
import com.stratio.decision.commons.avro.ColumnType;
import com.stratio.decision.commons.avro.InsertMessage;
import com.stratio.decision.commons.constants.StreamAction;
import com.stratio.decision.commons.messages.ColumnNameTypeValue;
import com.stratio.decision.commons.messages.StratioStreamingMessage;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by josepablofernandez on 16/03/16.
 */
public class JavaToAvroSerializer implements Serializer<StratioStreamingMessage, byte[]> {

    private static final long serialVersionUID = -3164736956154831507L;

    private SpecificDatumReader datumReader =  null;

    public JavaToAvroSerializer(SpecificDatumReader datumReader) {
        this.datumReader = datumReader;
    }

    @Override
    public byte[] serialize(StratioStreamingMessage object) {

        List<ColumnType> columns = null;

        if (object.getColumns() != null) {
            columns = new ArrayList<>();
            ColumnType c = null;

            for (ColumnNameTypeValue messageColumn : object.getColumns()) {

                if ( messageColumn.getValue() != null){

                    c = new ColumnType(messageColumn.getColumn(), messageColumn.getValue().toString(),
                            messageColumn.getValue().getClass().getName());

                } else {
                    c = new ColumnType(messageColumn.getColumn(), null, null);
                }

                columns.add(c);
            }

        }

        List<Action> actions = new ArrayList<>();

        if (object.getActiveActions() != null){

            object.getActiveActions().forEach( action -> {

                switch (action){
                    case LISTEN:
                        actions.add(Action.LISTEN);
                        break;
                    case SAVE_TO_CASSANDRA:
                        actions.add(Action.SAVE_TO_CASSANDRA) ;
                        break;
                    case SAVE_TO_ELASTICSEARCH:
                        actions.add(Action.SAVE_TO_ELASTICSEARCH) ;
                        break;
                    case SAVE_TO_MONGO:
                        actions.add(Action.SAVE_TO_MONGO) ;
                        break;
                    case SAVE_TO_SOLR:
                        actions.add(Action.SAVE_TO_SOLR) ;
                        break;
                    default: ;
                }

            });
        }

        InsertMessage insertMessage =  new InsertMessage(object.getOperation(), object.getStreamName(), object
                .getSession_id(), object.getTimestamp(), columns, actions);

        return getInsertMessageBytes(insertMessage);

    }


    private byte[] getInsertMessageBytes(InsertMessage insertMessage){

        byte[] result = null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        SpecificDatumWriter writer = new SpecificDatumWriter<InsertMessage>(InsertMessage.getClassSchema());

        try {
            writer.write(insertMessage, encoder);
            encoder.flush();
            out.close();
            result = out.toByteArray();
        }catch (IOException e){
            return null;
        }

        return result;

    }


    @Override
    public StratioStreamingMessage deserialize(byte[] object) {

        StratioStreamingMessage result = null;
        BinaryDecoder bd = DecoderFactory.get().binaryDecoder(object, null);

        if (datumReader == null) {
            datumReader =  new SpecificDatumReader(InsertMessage.getClassSchema());
        }
        try {
            InsertMessage insertMessage =  (InsertMessage) datumReader.read(null, bd);
            result = convertMessage(insertMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    private StratioStreamingMessage convertMessage(InsertMessage insertMessage){

        StratioStreamingMessage stratioStreamingMessage =  new StratioStreamingMessage();
        stratioStreamingMessage.setStreamName(insertMessage.getStreamName().toString());

        stratioStreamingMessage.setOperation(insertMessage.getOperation()==null?null:insertMessage.getOperation()
                .toString());
        stratioStreamingMessage.setSession_id(insertMessage.getSessionId()==null?null:insertMessage.getSessionId()
                .toString());

        stratioStreamingMessage.setTimestamp(insertMessage.getTimestamp());

        if (insertMessage.getActions() == null){
            stratioStreamingMessage.setActiveActions(null);
        } else {

            Set<StreamAction> activeActions = new HashSet<>();

            insertMessage.getActions().forEach(
                    action -> {

                        switch (action) {
                        case LISTEN:
                            activeActions.add(StreamAction.LISTEN);
                            break;
                        case SAVE_TO_CASSANDRA:
                            activeActions.add(StreamAction.SAVE_TO_CASSANDRA);
                            break;
                        case SAVE_TO_ELASTICSEARCH:
                            activeActions.add(StreamAction.SAVE_TO_ELASTICSEARCH);
                            break;
                        case SAVE_TO_MONGO:
                            activeActions.add(StreamAction.SAVE_TO_MONGO);
                            break;
                        case SAVE_TO_SOLR:
                            activeActions.add(StreamAction.SAVE_TO_SOLR);
                            break;
                        default:
                            ;
                        }

                    }
            );

            stratioStreamingMessage.setActiveActions(activeActions);
        }

        insertMessage.getData().forEach(

                data -> {

                    ColumnNameTypeValue columnNameTypeValue = new ColumnNameTypeValue();
                    columnNameTypeValue.setColumn(data.getColumn().toString());

                    if (data.getValue() == null){
                        columnNameTypeValue.setValue(null);
                        columnNameTypeValue.setType(null);
                    }
                    else {

                        switch (data.getType().toString()) {
                        case "java.lang.Double":
                            Double doubleData = new Double(data.getValue().toString());
                            columnNameTypeValue.setValue(doubleData);
                            columnNameTypeValue.setType(com.stratio.decision.commons.constants.ColumnType.DOUBLE);
                            break;
                        case "java.lang.Float":
                            Float floatData = new Float(data.getValue().toString());
                            columnNameTypeValue.setValue(floatData);
                            columnNameTypeValue.setType(com.stratio.decision.commons.constants.ColumnType.FLOAT);
                            break;
                        case "java.lang.Integer":
                            Integer integerData = new Integer(data.getValue().toString());
                            columnNameTypeValue.setValue(integerData);
                            columnNameTypeValue.setType(com.stratio.decision.commons.constants.ColumnType.INTEGER);
                            break;
                        case "java.lang.Long":
                            Long longData = new Long(data.getValue().toString());
                            columnNameTypeValue.setValue(longData);
                            columnNameTypeValue.setType(com.stratio.decision.commons.constants.ColumnType.LONG);
                            break;
                        case "java.lang.Boolean":
                            Boolean booleanData = new Boolean(data.getValue().toString());
                            columnNameTypeValue.setValue(booleanData);
                            columnNameTypeValue.setType(com.stratio.decision.commons.constants.ColumnType.BOOLEAN);
                            break;
                        default:
                            columnNameTypeValue.setValue(data.getValue().toString());
                            columnNameTypeValue.setType(com.stratio.decision.commons.constants.ColumnType.STRING);
                        }
                    }

                    stratioStreamingMessage.addColumn(columnNameTypeValue);
                }
        );


        return  stratioStreamingMessage;

    }

    @Override
    public List<byte[]> serialize(List<StratioStreamingMessage> object) {

        List<byte[]> result = new ArrayList<>();
        if (object != null) {
            for (StratioStreamingMessage message : object) {
                result.add(serialize(message));
            }
        }
        return result;
    }

    @Override
    public List<StratioStreamingMessage> deserialize(List<byte[]> object) {

        List<StratioStreamingMessage> result = new ArrayList<>();
        if (object != null) {
            for (byte[] event : object) {
                result.add(deserialize(event));
            }
        }
        return result;
    }


}
