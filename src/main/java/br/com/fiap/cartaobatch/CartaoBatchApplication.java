package br.com.fiap.cartaobatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import br.com.fiap.cartaobatch.entity.Cliente;

import javax.sql.DataSource;
import java.util.logging.Logger;

@SpringBootApplication
@EnableBatchProcessing
public class CartaoBatchApplication {

    Logger logger = Logger.getLogger(CartaoBatchApplication.class.getSimpleName());

    public static void main(String[] args) {
        SpringApplication.run(CartaoBatchApplication.class, args);
    }

    @Bean
    public ItemReader<Cliente> itemReader(@Value("${file.resource}") Resource resource){
        return new FlatFileItemReaderBuilder<Cliente>()
                .name("cliente file reader")
                .resource(resource)
                .targetType(Cliente.class)
                .comments("---")
                .recordSeparatorPolicy(new BlankLineRecordSeparatorPolicy())
                .lineTokenizer(new FixedLengthTokenizer() {{
                	setNames("nome", "matriculatxt", "turma");
                	setColumns(new Range(1, 41), new Range(42 , 48), new Range(50, 55));
                }})
                .build();
    }

    @Bean
    public ItemProcessor<Cliente, Cliente> itemProcessor(){
        return cliente -> {
//            logger.info("Processing: " + cliente.getNome());
//            logger.info("Matricula: " +cliente.getMatricula());
//            logger.info("turma: " + cliente.getTurma());
            if (cliente.getNome().contains("---")){
                return null;
            }
            cliente.setMatricula(Integer.parseInt(cliente.getMatriculaTxt()));
            cliente.setLimite(0.0);
            return cliente;

        };
    }

    @Bean
    public ItemWriter<Cliente> itemWriter(DataSource dataSource){
    	logger.info("Item writer: " + dataSource.toString());
        return new JdbcBatchItemWriterBuilder<Cliente>()
                .sql("insert into CLIENTES(nome, matricula, limite_disponivel, data_cadastro, data_atualizacao) values (:nome, :matricula, :limite, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory,
                     ItemReader<Cliente> itemReader,
                     ItemProcessor<Cliente, Cliente> itemProcessor,
                     ItemWriter<Cliente> itemWriter,
                     @Value("${file.chunk}") int chunkSize){
        return stepBuilderFactory.get("step chunk process txt")
                .<Cliente, Cliente>chunk(chunkSize)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory,
                   Step step){
        return jobBuilderFactory.get("job process file")
                .start(step)
                .build();
    }

}
