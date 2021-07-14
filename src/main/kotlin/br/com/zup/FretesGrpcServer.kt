package br.com.zup


import com.google.protobuf.Any
import com.google.rpc.Code
import com.google.rpc.StatusProto
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGrpcServer : FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FretesGrpcServer::class.java)

    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {
        logger.info("Calculando frete para request: $request")

        val cep = request?.cep

        if (cep.isNullOrBlank()) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("favor informar o cep")
                .asRuntimeException()
            responseObserver?.onError(e)
        }

        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("cep inválido")
                .augmentDescription("formato esperado deve ser: 00000-000")
                .asRuntimeException()
            responseObserver?.onError(e)
        }
        var valor = 0.0
        try {
            valor = Random.nextDouble(from = 0.0, until = 140.00)
            if (valor > 100) {
                throw IllegalStateException("Erro ao executar lógica de negócio")
            }
        } catch (e: Exception) {
            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription(e.message)
                    .withCause(e) // anexado ao status de erro, mas não enviado ao cliente
                    .asRuntimeException()
            )
        }

//        if (cep.endsWith("000")) {
//            val statusProto = com.google.rpc.Status.newBuilder()
//                .setCode(Code.CANCELLED_VALUE)
//                .setMessage("o valor não pode ser calculado")
//                .addDetails(
//                    Any.pack(
//                        ErroDetails.newBuilder()
//                            .setCode(422)
//                            .setMessage("Frete nãocoberto para a região")
//                            .build()))
//                .build()
//
//            val e = StatusProto.toStatusRuntimeException(statusProto)
//            responseObserver?.onError(e)
//
//        }

        val response = CalculaFreteResponse.newBuilder()
            .setValor(valor)
            .setCep(cep)
            .build()
        logger.info("Frete calculado: $response")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}