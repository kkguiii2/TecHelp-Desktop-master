package com.techelp.service;

import com.techelp.model.entity.Chamado;
import com.techelp.model.entity.Usuario;
import com.techelp.repository.ChamadoRepository;
import com.techelp.repository.UsuarioRepository;
import com.techelp.config.DatabaseConfig;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.time.LocalDate;

public class RelatorioService {
    
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public RelatorioService() {
        this.chamadoRepository = new ChamadoRepository();
        this.usuarioRepository = new UsuarioRepository();
    }
    
    public byte[] gerarRelatorioChamadosPorTecnico(Usuario tecnico, LocalDateTime inicio, LocalDateTime fim) {
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            
            document.open();
            adicionarCabecalho(document, "Relatório de Chamados por Técnico");
            
            List<Chamado> chamados = chamadoRepository.findByTecnicoEStatus(tecnico, Chamado.StatusChamado.FECHADO);
            
            // Tabela de estatísticas
            PdfPTable estatisticas = new PdfPTable(2);
            estatisticas.setWidthPercentage(100);
            
            adicionarLinha(estatisticas, "Total de Chamados", String.valueOf(chamados.size()));
            adicionarLinha(estatisticas, "Tempo Médio de Resolução", 
                calcularTempoMedioResolucao(chamados) + " horas");
            
            document.add(estatisticas);
            document.add(Chunk.NEWLINE);
            
            // Tabela de chamados
            PdfPTable tabela = new PdfPTable(5);
            tabela.setWidthPercentage(100);
            
            // Cabeçalho
            List<String> cabecalhos = Arrays.asList("ID", "Título", "Prioridade", "Data Abertura", "Tempo Resolução");
            for (String titulo : cabecalhos) {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setBorderWidth(2);
                header.setPhrase(new Phrase(titulo));
                tabela.addCell(header);
            }
            
            // Dados
            for (Chamado chamado : chamados) {
                tabela.addCell(chamado.getId().toString());
                tabela.addCell(chamado.getTitulo());
                tabela.addCell(chamado.getPrioridade().toString());
                tabela.addCell(DATE_FORMATTER.format(chamado.getDataAbertura()));
                tabela.addCell(chamado.getTempoResolucao() + "h");
            }
            
            document.add(tabela);
            document.close();
            
            return out.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório", e);
        }
    }
    
    public byte[] gerarRelatorioDesempenho(LocalDateTime inicio, LocalDateTime fim) {
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            
            document.open();
            adicionarCabecalho(document, "Relatório de Desempenho");
            
            // Estatísticas gerais
            Map<Chamado.StatusChamado, Long> chamadosPorStatus = chamadoRepository.findByPeriodo(inicio, fim)
                .stream()
                .collect(Collectors.groupingBy(Chamado::getStatus, Collectors.counting()));
                
            PdfPTable estatisticas = new PdfPTable(2);
            estatisticas.setWidthPercentage(100);
            
            for (Map.Entry<Chamado.StatusChamado, Long> entry : chamadosPorStatus.entrySet()) {
                adicionarLinha(estatisticas, entry.getKey().toString(), entry.getValue().toString());
            }
            
            document.add(estatisticas);
            document.add(Chunk.NEWLINE);
            
            // Gráfico de categorias
            Map<String, Long> chamadosPorCategoria = chamadoRepository.contarChamadosPorCategoria()
                .stream()
                .collect(Collectors.toMap(
                    arr -> (String) arr[0],
                    arr -> (Long) arr[1]
                ));
                
            PdfPTable categorias = new PdfPTable(2);
            categorias.setWidthPercentage(100);
            
            for (Map.Entry<String, Long> entry : chamadosPorCategoria.entrySet()) {
                adicionarLinha(categorias, entry.getKey(), entry.getValue().toString());
            }
                
            document.add(categorias);
            document.close();
            
            return out.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório", e);
        }
    }
    
    public byte[] gerarRelatorioPorCategoria(String categoria, LocalDateTime inicio, LocalDateTime fim) {
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            
            document.open();
            adicionarCabecalho(document, "Relatório de Chamados - Categoria: " + categoria);
            
            List<Chamado> chamados = chamadoRepository.findByPeriodo(inicio, fim).stream()
                    .filter(c -> c.getCategoriaIa().equals(categoria))
                    .collect(Collectors.toList());
            
            // Tabela de estatísticas
            PdfPTable estatisticas = new PdfPTable(2);
            estatisticas.setWidthPercentage(100);
            
            adicionarLinha(estatisticas, "Total de Chamados", String.valueOf(chamados.size()));
            adicionarLinha(estatisticas, "Tempo Médio de Resolução", 
                calcularTempoMedioResolucao(chamados) + " horas");
            
            document.add(estatisticas);
            document.add(Chunk.NEWLINE);
            
            // Tabela de chamados
            PdfPTable tabela = new PdfPTable(5);
            tabela.setWidthPercentage(100);
            
            // Cabeçalho
            List<String> cabecalhos = Arrays.asList("ID", "Título", "Status", "Data Abertura", "Tempo Resolução");
            for (String titulo : cabecalhos) {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setBorderWidth(2);
                header.setPhrase(new Phrase(titulo));
                tabela.addCell(header);
            }
            
            // Dados
            for (Chamado chamado : chamados) {
                tabela.addCell(chamado.getId().toString());
                tabela.addCell(chamado.getTitulo());
                tabela.addCell(chamado.getStatus().toString());
                tabela.addCell(DATE_FORMATTER.format(chamado.getDataAbertura()));
                tabela.addCell(chamado.getTempoResolucao() != null ? 
                    chamado.getTempoResolucao() + "h" : "Em aberto");
            }
            
            document.add(tabela);
            document.close();
            
            return out.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório", e);
        }
    }
    
    private void adicionarCabecalho(Document document, String titulo) throws DocumentException {
        Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
        Paragraph paragrafo = new Paragraph(titulo, fonteTitulo);
        paragrafo.setAlignment(Element.ALIGN_CENTER);
        paragrafo.setSpacingAfter(20);
        document.add(paragrafo);
    }
    
    private void adicionarLinha(PdfPTable tabela, String chave, String valor) {
        PdfPCell cellChave = new PdfPCell(new Phrase(chave));
        PdfPCell cellValor = new PdfPCell(new Phrase(valor));
        
        cellChave.setBorderWidth(1);
        cellValor.setBorderWidth(1);
        
        tabela.addCell(cellChave);
        tabela.addCell(cellValor);
    }
    
    private double calcularTempoMedioResolucao(List<Chamado> chamados) {
        return chamados.stream()
                .mapToLong(Chamado::getTempoResolucao)
                .average()
                .orElse(0.0);
    }
    
    public int getTotalChamadosPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return chamadoRepository.countByPeriodo(inicio, fim);
    }
    
    public double getTempoMedioResolucao(LocalDateTime inicio, LocalDateTime fim) {
        return chamadoRepository.getTempoMedioResolucao(inicio, fim);
    }
    
    public double getMediaAvaliacoes(LocalDateTime inicio, LocalDateTime fim) {
        return chamadoRepository.getMediaAvaliacoes(inicio, fim);
    }
    
    public Map<Chamado.StatusChamado, Long> getChamadosPorStatus(LocalDateTime inicio, LocalDateTime fim) {
        return chamadoRepository.countByStatus(inicio, fim);
    }
    
    public Map<String, Long> getChamadosPorCategoria(LocalDateTime inicio, LocalDateTime fim) {
        return chamadoRepository.countByCategoria(inicio, fim);
    }
    
    public List<Chamado> getChamadosPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return chamadoRepository.findByPeriodo(inicio, fim);
    }
    
    public void exportarRelatorio(LocalDate dataInicio, LocalDate dataFim) {
        // TODO: Implementar exportação do relatório
        throw new UnsupportedOperationException("Exportação de relatório ainda não implementada");
    }
    
    public int getTotalChamados(LocalDateTime inicio, LocalDateTime fim) {
        return chamadoRepository.countByPeriodo(inicio, fim);
    }
    
    public double getSatisfacaoMedia(LocalDateTime inicio, LocalDateTime fim) {
        return chamadoRepository.getMediaAvaliacoes(inicio, fim);
    }
    
    public Map<LocalDate, Long> getChamadosPorDia(LocalDateTime inicio, LocalDateTime fim) {
        return chamadoRepository.countByDia(inicio, fim);
    }
    
    public List<String> getTodasCategorias() {
        return chamadoRepository.findAll().stream()
            .map(Chamado::getCategoriaIa)
            .distinct()
            .collect(Collectors.toList());
    }
    
    public void gerarRelatorioPorTecnico(Usuario tecnico, LocalDate dataInicio, LocalDate dataFim) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("relatorio_tecnico_" + tecnico.getId() + ".pdf"));
            document.open();
            
            document.add(new Paragraph("Relatório de Chamados - Técnico: " + tecnico.getNome()));
            document.add(new Paragraph("Período: " + dataInicio.format(dateFormatter) + 
                " a " + dataFim.format(dateFormatter)));
            
            List<Chamado> chamados = chamadoRepository.findByTecnicoEPeriodo(
                tecnico,
                dataInicio.atStartOfDay(),
                dataFim.atTime(23, 59, 59)
            );
            
            for (Chamado chamado : chamados) {
                document.add(new Paragraph("\nChamado #" + chamado.getId()));
                document.add(new Paragraph("Título: " + chamado.getTitulo()));
                document.add(new Paragraph("Status: " + chamado.getStatus()));
                document.add(new Paragraph("Prioridade: " + chamado.getPrioridade()));
                document.add(new Paragraph("Data Abertura: " + 
                    chamado.getDataAbertura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                if (chamado.getDataFechamento() != null) {
                    document.add(new Paragraph("Data Fechamento: " + 
                        chamado.getDataFechamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                }
            }
            
            document.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório por técnico: " + e.getMessage(), e);
        }
    }
    
    public void gerarRelatorioDesempenho(LocalDate dataInicio, LocalDate dataFim) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("relatorio_desempenho.pdf"));
            document.open();
            
            document.add(new Paragraph("Relatório de Desempenho"));
            document.add(new Paragraph("Período: " + dataInicio.format(dateFormatter) + 
                " a " + dataFim.format(dateFormatter)));
            
            LocalDateTime inicio = dataInicio.atStartOfDay();
            LocalDateTime fim = dataFim.atTime(23, 59, 59);
            
            List<Chamado> chamados = chamadoRepository.findByPeriodo(inicio, fim);
            
            // Estatísticas gerais
            long totalChamados = chamados.size();
            long chamadosFechados = chamados.stream()
                .filter(c -> c.getStatus() == Chamado.StatusChamado.FECHADO)
                .count();
            
            double tempoMedioResolucao = chamados.stream()
                .filter(c -> c.getStatus() == Chamado.StatusChamado.FECHADO)
                .mapToLong(Chamado::getTempoResolucao)
                .average()
                .orElse(0.0);
                
            document.add(new Paragraph("\nEstatísticas Gerais:"));
            document.add(new Paragraph("Total de Chamados: " + totalChamados));
            document.add(new Paragraph("Chamados Fechados: " + chamadosFechados));
            document.add(new Paragraph("Tempo Médio de Resolução: " + 
                String.format("%.2f horas", tempoMedioResolucao / 3600.0)));
            
            document.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de desempenho: " + e.getMessage(), e);
        }
    }
    
    public void gerarRelatorioPorCategoria(String categoria, LocalDate dataInicio, LocalDate dataFim) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("relatorio_categoria_" + categoria + ".pdf"));
            document.open();
            
            document.add(new Paragraph("Relatório de Chamados - Categoria: " + categoria));
            document.add(new Paragraph("Período: " + dataInicio.format(dateFormatter) + 
                " a " + dataFim.format(dateFormatter)));
            
            List<Chamado> chamados = chamadoRepository.findByCategoriaEPeriodo(
                categoria,
                dataInicio.atStartOfDay(),
                dataFim.atTime(23, 59, 59)
            );
            
            for (Chamado chamado : chamados) {
                document.add(new Paragraph("\nChamado #" + chamado.getId()));
                document.add(new Paragraph("Título: " + chamado.getTitulo()));
                document.add(new Paragraph("Status: " + chamado.getStatus()));
                document.add(new Paragraph("Prioridade: " + chamado.getPrioridade()));
                document.add(new Paragraph("Técnico: " + 
                    (chamado.getTecnico() != null ? chamado.getTecnico().getNome() : "Não atribuído")));
                document.add(new Paragraph("Data Abertura: " + 
                    chamado.getDataAbertura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            }
            
            document.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório por categoria: " + e.getMessage(), e);
        }
    }
} 