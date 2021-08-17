package br.com.zup.edu

import com.google.protobuf.Any
import com.google.rpc.Code
import com.google.rpc.StatusProto
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGrpcServer : FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FretesGrpcServer::class.java)

    override fun calculaFrete(request: FreteRequest?, responseObserver: StreamObserver<FreteResponse>?) {
        logger.info("Calculando frete para request: $request")

        /**
         * Cep Obrigatório
         */
        val cep = request?.cep
        if (cep == null || cep.isBlank()) {

            val erro = Status.INVALID_ARGUMENT
                .withDescription("cep deve ser informado")
                .asRuntimeException()

            responseObserver?.onError(erro)
        }
        /**
         * Verificando se o cep é válido
         */
        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {

            val erro = Status.INVALID_ARGUMENT
                .withDescription("cep inválido")
                .augmentDescription("formato esperado deve ser 99999-999")
                .asRuntimeException()

            responseObserver?.onError(erro)
        }

        /**
         * Simular verificação de segurança
         */
        if (cep.endsWith("333")) {

            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("Usuário não pode acessar esse recurso")
                .addDetails(
                    Any.pack(
                        ErrorDetails.newBuilder()
                            .setCode(401)
                            .setMessage("token expirado")
                            .build()
                    )
                )
                .build()
            val e = io.grpc.protobuf.StatusProto.toStatusRuntimeException(statusProto)
            responseObserver?.onError(e)
        }

        /**
         * Erro de negócio
         */
        var valor = 0.0
        try {
            valor = Random.nextDouble(from = 0.0, until = 140.00)
            if (valor > 100.0) {
                throw IllegalStateException("Erro inesperado ao executar lógica de negócio ")
            }
        } catch (e: Exception) {
            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription(e.message)
                    .withCause(e)
                    .asRuntimeException()
            )
        }


        val response = FreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setValor(valor)
            .build()

        logger.info("Frete Calculado: $response")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}