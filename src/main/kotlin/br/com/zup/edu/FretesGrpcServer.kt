package br.com.zup.edu

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGrpcServer:FretesServiceGrpc.FretesServiceImplBase(){

    private val logger = LoggerFactory.getLogger(FretesGrpcServer::class.java)

    override fun calculaFrete(request: FreteRequest?, responseObserver: StreamObserver<FreteResponse>?) {
        logger.info("Calculando frete para request: $request")

        /**
         * Cep Obrigatório
         */
        val cep = request?.cep
        if(cep == null || cep.isBlank()){

            val erro = Status.INVALID_ARGUMENT
               .withDescription("cep deve ser informado")
               .asRuntimeException()

            responseObserver?.onError(erro)
        }


        val response = FreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setValor(Random.nextDouble(from = 0.0, until = 140.00))
            .build()

        logger.info("Frete Calculado: $response")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}