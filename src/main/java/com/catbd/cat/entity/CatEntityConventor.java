package com.catbd.cat.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SqlTypes;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CatEntityConventor implements UserType<CatEntity> {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public int getSqlType() {
        return SqlTypes.JSON;
    }

    @Override
    public Class<CatEntity> returnedClass() {
        return CatEntity.class;
    }

    @Override
    public boolean equals(CatEntity catEntity, CatEntity j1) {
        return false;
    }

    @Override
    public int hashCode(CatEntity catEntity) {
        return 0;
    }

    @Override
    public CatEntity nullSafeGet(ResultSet resultSet, int i, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws SQLException {
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, CatEntity catEntity, int i, SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException {

    }

    @Override
    public CatEntity deepCopy(CatEntity catEntity) {
        return null;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(CatEntity catEntity) {
        return null;
    }

    @Override
    public CatEntity assemble(Serializable serializable, Object o) {
        return null;
    }

}
