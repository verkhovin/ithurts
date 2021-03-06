package dev.ithurts.configuration

import dev.ithurts.application.service.internal.diff.advancedbinding.JavaBindingService
import dev.ithurts.application.service.internal.diff.advancedbinding.KotlinBindingService
import dev.ithurts.application.service.internal.diff.advancedbinding.LanguageSpecificBindingService
import dev.ithurts.application.service.internal.diff.advancedbinding.code.JavaCodeAnalyzer
import dev.ithurts.application.service.internal.diff.advancedbinding.code.KotlinCodeAnalyzer
import dev.ithurts.domain.CostCalculationService
import dev.ithurts.domain.Language
import dev.ithurts.git.GitDiffAnalyzer
import dev.ithurts.git.HunkResolvingStrategy
import io.reflectoring.diffparser.api.DiffParser
import io.reflectoring.diffparser.api.UnifiedDiffParser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import java.time.Clock


@Configuration
@EnableMongoAuditing
class ApplicationConfiguration {
    @Bean
    fun languageSpecificBindingServices(
        kotlinCodeAnalyzer: KotlinCodeAnalyzer,
        javaCodeAnalyzer: JavaCodeAnalyzer,
    ): Map<Language, LanguageSpecificBindingService> {
        return mapOf(
            Language.KOTLIN to KotlinBindingService(kotlinCodeAnalyzer),
            Language.JAVA to JavaBindingService(javaCodeAnalyzer)
        )
    }

    @Bean
    fun costCalculationService(): CostCalculationService {
        return CostCalculationService()
    }

    @Bean
    fun clock(): Clock {
        return Clock.systemUTC()
    }

    @Bean
    fun diffParser() : DiffParser {
        return UnifiedDiffParser()
    }

    @Bean
    fun gitDiffAnalyzer(): GitDiffAnalyzer {
        return GitDiffAnalyzer(HunkResolvingStrategy())
    }
}